package com.example.jobfinderapp.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jobfinderapp.utils.JobCountries
import com.example.jobfinderapp.entity.JobFilter
import com.example.jobfinderapp.domain.InterActor
import com.example.jobfinderapp.entity.Category
import com.example.jobfinderapp.entity.Country
import com.example.jobfinderapp.data.entity.Job
import com.example.jobfinderapp.entity.Company
import com.example.jobfinderapp.entity.HomeUiState
import com.example.jobfinderapp.entity.JobSortType
import com.example.jobfinderapp.entity.Location
import com.example.jobfinderapp.entity.Result
import com.example.jobfinderapp.utils.AppLogger
import com.example.jobfinderapp.utils.AppPrefs
import com.example.jobfinderapp.utils.NetworkMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class HomeFragViewModel @Inject constructor(private val interActor: InterActor, private val networkMonitor: NetworkMonitor, private val appPrefs: AppPrefs) : ViewModel() {

    //DB
    private val jobSortTypeFlow = MutableStateFlow<JobSortType>(JobSortType.DEFAULT)

    @OptIn(ExperimentalCoroutinesApi::class)
    val jobUIModelFlow = jobSortTypeFlow
        .flatMapLatest { sortType ->
            when(sortType) {
                JobSortType.DEFAULT -> interActor.getJobsUIModelDB()
                JobSortType.SALARY_ASC -> interActor.getJobsBySalaryAscDB()
                JobSortType.SALARY_DESC -> interActor.getJobsBySalaryDescDB()
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    private var wasOffline = false

    //текущий фильтр
    private val _filter = MutableStateFlow<JobFilter>(createDefaultFilter())
    val filter: StateFlow<JobFilter> = _filter.asStateFlow()

    //состояние интернета
    val internetState: StateFlow<Boolean> = networkMonitor.isConnected

    val homeUIStateFlow: StateFlow<HomeUiState> = combine(internetState, jobUIModelFlow) { isConn, jobUIModelList ->

        HomeUiState(
            jobs = jobUIModelList,
            isConnected = isConn,
            showNoResults = isConn && jobUIModelList.isEmpty(),
            showNoInternet = !isConn && jobUIModelList.isEmpty()
        )

    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        HomeUiState()
    )

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

    private val _hasPendingFilterChanges = MutableStateFlow<Boolean>(false)
    val hasPendingFilterChanges: StateFlow<Boolean> = _hasPendingFilterChanges.asStateFlow()

    //State for pagination
    var isLoading = false
    var isLastPage = false
    var currentPage = 1

    private val PAGE_SIZE = 10

    //Чтобы показать, сколько фильтров установлено, только после того, как фильтр был применен. То есть нажата кнопка search
    private val _isFilterInstalled = MutableStateFlow<Boolean>(false)

    private var lastAppliedFilter: JobFilter? = null

    val filterBadgeCountFlow: StateFlow<Int> = _isFilterInstalled.combine(_filter) { isInstalled, filter ->
        if (isInstalled) getSelectedFilterCount(filter)
        else 0
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        0
    )

    private val _searchKeyWordsFlow = MutableStateFlow<String?>(_filter.value.searchKeyWords)

    init {
        loadFilteredJobList(_filter.value)

        viewModelScope.launch {
            networkMonitor.isConnected.collect { connected ->
                if (!connected) {
                    wasOffline = true
                }
                if (connected && wasOffline) {
                    wasOffline = false
                    loadNextPage()
                }
            }
        }

        viewModelScope.launch {
            selectedCountryFromSharedPrefFlow.collect { newCountryCode ->
                val currentFilter = _filter.value

                if (currentFilter.country.code != newCountryCode) {
                    val updatedFilter = currentFilter.copy(
                        country = Country(
                            getCountryNameByCode(newCountryCode),
                            newCountryCode
                        ),
                        locations = emptyList()
                    )

                    _filter.value = updatedFilter
                    loadFilteredJobList(updatedFilter)
                }
            }
        }

        viewModelScope.launch {
            _searchKeyWordsFlow
                .debounce(400)
                .distinctUntilChanged()
                .collectLatest { query ->
                    val curr = _filter.value

                    if (curr.searchKeyWords != query ) {
                        val newFilter = curr.copy(searchKeyWords = query)
                        _filter.update { newFilter }

                        loadFilteredJobList(newFilter)
                    }
                }
        }
    }

    private fun observeJobsFromDb(sortType: JobSortType) {
        jobSortTypeFlow.update {
            sortType
        }
    }

    private fun createDefaultFilter(): JobFilter {
        val countryCode = appPrefs.getSelectedCountry()
        val countryName = getCountryNameByCode(countryCode)

        val defaultDirection = JobSortType.DEFAULT

        return JobFilter(
            country = Country(countryName, countryCode),
            sortBy = defaultDirection
        )
    }

    fun getCountryNameByCode(countryCode: String): String =
        JobCountries.countriesMapNorm[countryCode] ?: "Great Britain"

    fun applyFilter() {
        val filter = _filter.value

        loadFilteredJobList(filter)
        observeJobsFromDb(filter.sortBy)

        _hasPendingFilterChanges.update {
            false
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
                _hasPendingFilterChanges.value = true
                curr.copy(onlyFullTime = checked)
            } else {
                curr
            }
        }
    }

    fun onPartTimeChecked(checked: Boolean) {
        _filter.update { curr ->
            if (curr.onlyPartTime != checked) {
                _hasPendingFilterChanges.value = true
                curr.copy(onlyPartTime = checked)
            } else {
                curr
            }
        }
    }

    fun onContractChecked(checked: Boolean) {
        _filter.update { curr ->
            if (curr.onlyContractJobs != checked) {
                _hasPendingFilterChanges.value = true
                curr.copy(onlyContractJobs = checked)
            } else {
                curr
            }
        }
    }

    fun onPermanentChecked(checked: Boolean) {
        _filter.update { curr ->
            if (curr.onlyPermanentJobs != checked) {
                _hasPendingFilterChanges.value = true
                curr.copy(onlyPermanentJobs = checked)
            } else {
                curr
            }
        }
    }

    fun onCategoryChecked(category: Category) {
        _filter.update { curr ->
            if (curr.category != category) {
                _hasPendingFilterChanges.value = true
                curr.copy(category = category)
            } else {
                curr
            }
        }
    }

    fun onLocationChecked(locations: List<String>) {
        _filter.update { curr ->
            if (curr.locations != locations) {
                _hasPendingFilterChanges.value = true
                curr.copy(locations = locations)
            } else {
                curr
            }
        }
    }

    fun onCountryChanged(country: Country) {
        _filter.update { curr ->
            if (curr.country.code != country.code) {
                _hasPendingFilterChanges.value = true
                curr.copy(country = country)
            } else {
                curr
            }
        }
    }

    fun onSortByChecked(sortType: JobSortType) {
        _filter.update { curr ->
            if (curr.sortBy != sortType) {
                _hasPendingFilterChanges.value = true
                curr.copy(sortBy = sortType)
            } else {
                curr
            }
        }
    }

    fun onSearchKeyWordsChangedFlow(words: String?) {
        _searchKeyWordsFlow.update {
            words
        }
    }

    fun resetFilter() {
        _filter.value = createDefaultFilter()
        lastAppliedFilter = createDefaultFilter()
        loadFilteredJobList(lastAppliedFilter ?: createDefaultFilter())

        _hasPendingFilterChanges.update { false }
    }

    private fun loadPage(filter: JobFilter) {

        if (isLoading || isLastPage) return

        isLoading = true

        viewModelScope.launch {

            try {
                val result = interActor.getFilteredJobsFromRepo(filter, currentPage)
                if (result.isSuccessful) {
                    val body = result.body()
                    if (body != null) {

                        val resList = body.results
                        val filteredJobList = releaseNullableFromDB(resList)

                        if (currentPage == 1) {
                            //DB
                            interActor.clearAndInsertJobsDB(filteredJobList)
                        } else {
                            //DB
                            interActor.refreshJobs(filteredJobList)
                        }

                        if (filteredJobList.size < PAGE_SIZE) {
                            isLastPage = true
                        } else {
                            currentPage++
                        }

                    } else {
                        AppLogger.e("HomeFragmentViewModel", "No results found for the specified filter")
                    }
                } else {
                    AppLogger.e("HomeFragmentViewModel", "${result.errorBody()}")
                }
            } catch (e: Exception) {
                AppLogger.e("HomeFragmentViewModel", "Network error! ${e.message}", e)
            }

            isLoading = false
        }

    }

    fun loadFilteredJobList(filter: JobFilter) {

        val countryCode = filter.country.code
        val countryLocCode = JobCountries.countriesLocationCode[countryCode] ?: "UK"

        val finalLocation = mutableListOf<String>()
        finalLocation.add(countryLocCode)
        finalLocation.addAll(filter.locations ?: emptyList())

        val finalFilter = filter.copy(locations = finalLocation)

        lastAppliedFilter = finalFilter

        currentPage = 1
        isLastPage = false

        loadPage(finalFilter)
    }

    fun loadNextPage() {
        val filter = lastAppliedFilter ?: createDefaultFilter()
        loadPage(filter)
    }

    //В бд нельзя ложить данные, если они указаны как non-nullable, а в Job по умолчанию такие есть, поэтому приходиться применять вот этот метод.
    fun releaseNullableFromDB(resList: List<Result>) =
        resList.map { resItem ->

            val id = resItem.id
            val title = resItem.title
            val description = resItem.description

            // 2. Глубокая проверка Location (даже если сам объект пришел, поля внутри могут быть null)
            val location = Location(
                area = resItem.location?.area ?: listOf(""),
                display_name = resItem.location?.display_name ?: "Not specified"
            )

            // 3. Глубокая проверка Category
            val category = Category(
                label = resItem.category?.label ?: "Not specified",
                tag = resItem.category?.tag ?: ""
            )

            // 4. Глубокая проверка Company
            val company = Company(
                average_salary = resItem.company?.average_salary ?: 0f,
                canonical_name = resItem.company?.canonical_name ?: "",
                count = resItem.company?.count ?: 0,
                display_name = resItem.company?.display_name ?: "Not specified"
            )

            val salaryMax = resItem.salary_max ?: 0f
            val salaryMin = resItem.salary_min ?: 0f

            val contractTime = when(resItem.contract_time) {
                "full_time", null -> "Full-time"
                "part_time" -> "Part-time"
                else -> "Full-time"
            }

            val contractType = when(resItem.contract_type) {
                "permanent", null -> "Permanent"
                "contract" -> "Contract"
                else -> "Permanent"
            }

            val date = resItem.created.split("T", "Z")
            val a = date[1].split(":")
            val b = "${a[0]}:${a[1]}"

            val createdTime = "Posted ${date[0]} $b"

            Job(
                id = id,
                title = title,
                description = description,
                createdTime = createdTime,
                location = location,
                category = category,
                company = company,
                salary_max = salaryMax,
                salary_min = salaryMin,
                contract_time = contractTime,
                contract_type = contractType
            )
        }
}