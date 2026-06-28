package com.example.jobfinderapp.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.example.jobfinderapp.utils.JobCountries
import com.example.jobfinderapp.entity.JobFilter
import com.example.jobfinderapp.domain.InterActor
import com.example.jobfinderapp.entity.Category
import com.example.jobfinderapp.entity.Country
import com.example.jobfinderapp.data.entity.Job
import com.example.jobfinderapp.utils.AppPrefs
import com.example.jobfinderapp.utils.NetworkMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class HomeFragViewModel @Inject constructor(private val interActor: InterActor, private val networkMonitor: NetworkMonitor, private val appPrefs: AppPrefs) : ViewModel() {

    //текущий фильтр
    private val _filter = MutableStateFlow<JobFilter>(createDefaultFilter())
    val filter = _filter.asStateFlow()

    //примененный фильтр
    private val _appliedFilter = MutableStateFlow<JobFilter>(createDefaultFilter())

    //при смене фильтров перейти в начало списка
    private val _scrollToTop = Channel<Unit>(Channel.BUFFERED)
    val scrollToTop = _scrollToTop.receiveAsFlow()

    private val _jobSortDirection = MutableStateFlow<String?>(null)
    private val _jobSortBy = MutableStateFlow<String?>(null)

    // Потокобезопасный стейт для фиксации офлайна
    private val _wasOffline = MutableStateFlow(false)

    // Горячий поток для разовых событий (микрофон приложения)
    private val _networkRestoredEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val networkRestoredEvent = _networkRestoredEvent.asSharedFlow()

    //DB
    @OptIn(ExperimentalCoroutinesApi::class)
    val jobUIModelFlow = _appliedFilter
        .map(::prepareFilter)
        .distinctUntilChanged()
        .flatMapLatest {
            interActor.getJobsUIModelDB(it)
        }
        .cachedIn(viewModelScope)

    //переменная для подписки на изменения выбора страны из настроек через sharedPref
    private val selectedCountryFromSharedPrefFlow = appPrefs.observeSelectedCountryFlow()

    //в сравнение не берем такие параметры как, locations и searchKeyWords
    val filterState = _filter.map { current ->
        val default = createDefaultFilter()

        val isBaseChanged = current.copy(
            locations = default.locations,
            searchKeyWords = default.searchKeyWords
        ) != default

        val hasUserLocations = (current.locations?.size ?: 0) > 1

        isBaseChanged || hasUserLocations
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        false
    )

    //Для управления состоянием filterBottomSheet search button
    val enableSearchButton = combine(_appliedFilter, _filter) { lastAppliedFilter, currentFilter ->
        lastAppliedFilter != currentFilter
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        false
    )

    //Чтобы показать, сколько фильтров установлено, только после того, как фильтр был применен. То есть нажата кнопка search
    private val _isFilterInstalled = MutableStateFlow<Boolean>(false)

    //если фильтр применен, то посчитать сколько фильтров выбрано было.
    val filterBadgeCountFlow: StateFlow<Int> = _isFilterInstalled.combine(_appliedFilter) { isInstalled, filter ->
        if (isInstalled) getSelectedFilterCount(filter)
        else 0
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        0
    )

    //для search view
    private val _searchKeyWordsFlow = MutableStateFlow<String?>(_filter.value.searchKeyWords)

    init {
        viewModelScope.launch {
            networkMonitor.isConnected.collect { connected ->
                if (!connected) {
                    // Используем .update для атомарного и безопасного изменения
                    _wasOffline.update { true }
                }

                // Проверяем актуальное значение атомарно
                if (connected && _wasOffline.value) {
                    _wasOffline.update { false }
                    // Стреляем событием «Сеть восстановлена!»
                    _networkRestoredEvent.tryEmit(Unit)
                }
            }
        }

        viewModelScope.launch {
            selectedCountryFromSharedPrefFlow.collectLatest { newCountryCode ->
                val currentFilter = _appliedFilter.value

                val updatedFilter = currentFilter.copy(
                    country = Country(
                        getCountryNameByCode(newCountryCode),
                        newCountryCode
                    ),
                    locations = emptyList()
                )

                _appliedFilter.value = updatedFilter
                _filter.value = updatedFilter

                viewModelScope.launch {
                    _scrollToTop.send(Unit)
                }
            }
        }

        viewModelScope.launch {
            _searchKeyWordsFlow
                .debounce(400)
                .distinctUntilChanged()
                .collectLatest { query ->
                    val curr = _appliedFilter.value

                    val newFilter = curr.copy(searchKeyWords = query)

                    _appliedFilter.value = newFilter
                    _filter.value = newFilter

                    viewModelScope.launch {
                        _scrollToTop.send(Unit)
                    }
                }
        }
    }

    private fun prepareFilter(filter: JobFilter): JobFilter {
        val countryCode = filter.country.code
        val countryLocCode = JobCountries.countriesLocationCode[countryCode] ?: "UK"

        val finalLocation = mutableListOf<String>()
        finalLocation.add(countryLocCode)
        finalLocation.addAll(filter.locations ?: emptyList())

        val finalFilter = filter.copy(locations = finalLocation)

        return finalFilter
    }

    private fun updateSortDirection(sortDirection: String?) {
        _jobSortDirection.value = sortDirection
    }

    private fun updateSortBy(sortBy: String?) {
        _jobSortBy.value = sortBy
    }

    private fun createDefaultFilter(): JobFilter {
        val countryCode = appPrefs.getSelectedCountry()
        val countryName = getCountryNameByCode(countryCode)

        return JobFilter(
            country = Country(countryName, countryCode)
        )
    }

    fun getCountryNameByCode(countryCode: String): String =
        JobCountries.countriesMapNorm[countryCode] ?: "Great Britain"

    fun applyFilter() {
        val filter = _filter.value

        updateSortBy(filter.sortBy)
        updateSortDirection(filter.sortDirection)

        _appliedFilter.value = filter

        viewModelScope.launch {
            _scrollToTop.send(Unit)
        }
    }

    fun installFilter(isInstalled: Boolean) {
        _isFilterInstalled.update {
            isInstalled
        }
    }

    fun getSelectedFilterCount(currFilter: JobFilter): Int {
        val defFiler = createDefaultFilter()

        var resCount = 0

        if (currFilter.onlyFullTime != defFiler.onlyFullTime) resCount++
        if (currFilter.onlyPartTime != defFiler.onlyPartTime) resCount++
        if (currFilter.onlyContractJobs != defFiler.onlyContractJobs) resCount++
        if (currFilter.onlyPermanentJobs != defFiler.onlyPermanentJobs) resCount++
        if (currFilter.category != defFiler.category) resCount++
        if (!currFilter.locations.isNullOrEmpty() && currFilter.locations.size > 1) resCount++
        if (currFilter.country != defFiler.country) resCount++
        if (currFilter.sortBy != defFiler.sortBy) resCount++
        if (currFilter.sortDirection != defFiler.sortDirection) resCount++

        return resCount
    }

    fun toggleSaved(job: Job) {
        viewModelScope.launch {
            interActor.toggleSaved(job)
        }
    }

    fun onFullTimeChecked(checked: Boolean) {
        _filter.update { curr ->
            if (curr.onlyFullTime != checked) {
                curr.copy(onlyFullTime = checked)
            } else {
                curr
            }
        }
    }

    fun onPartTimeChecked(checked: Boolean) {
        _filter.update { curr ->
            if (curr.onlyPartTime != checked) {
                curr.copy(onlyPartTime = checked)
            } else {
                curr
            }
        }
    }

    fun onContractChecked(checked: Boolean) {
        _filter.update { curr ->
            if (curr.onlyContractJobs != checked) {
                curr.copy(onlyContractJobs = checked)
            } else {
                curr
            }
        }
    }

    fun onPermanentChecked(checked: Boolean) {
        _filter.update { curr ->
            if (curr.onlyPermanentJobs != checked) {
                curr.copy(onlyPermanentJobs = checked)
            } else {
                curr
            }
        }
    }

    fun onCategoryChecked(category: Category) {
        _filter.update { curr ->
            if (curr.category != category) {
                curr.copy(category = category)
            } else {
                curr
            }
        }
    }

    fun onLocationChecked(locations: List<String>) {
        _filter.update { curr ->
            if (curr.locations != locations) {
                curr.copy(locations = locations)
            } else {
                curr
            }
        }
    }

    fun onCountryChanged(country: Country) {
        _filter.update { curr ->
            if (curr.country.code != country.code) {
                curr.copy(country = country)
            } else {
                curr
            }
        }
    }

    fun onSortByChanged(sortBy: Boolean) {
        val sortByString = if (sortBy) "salary" else null

        _filter.update { curr ->
            if(curr.sortBy != sortByString) {
                curr.copy(sortBy = sortByString)
            } else {
                curr
            }
        }
    }

    fun onSortDirectionChanged(sortDirection: String?) {
        _filter.update { curr ->
            if (curr.sortDirection != sortDirection) {
                curr.copy(sortDirection = sortDirection)
            } else {
                curr
            }
        }

    }

    fun onSearchKeyWordsChangedFlow(words: String?) {
        _searchKeyWordsFlow.value = words
    }

    fun resetFilter() {
        val defaultFilter = createDefaultFilter()
        _filter.value = defaultFilter
       _appliedFilter.value = defaultFilter

        _isFilterInstalled.value = false
    }
}