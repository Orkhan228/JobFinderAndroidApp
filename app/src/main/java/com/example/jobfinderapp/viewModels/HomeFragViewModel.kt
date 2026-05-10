package com.example.jobfinderapp.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.jobfinderapp.utils.JobCountries
import com.example.jobfinderapp.entity.JobFilter
import com.example.jobfinderapp.domain.InterActor
import com.example.jobfinderapp.entity.Category
import com.example.jobfinderapp.entity.Country
import com.example.jobfinderapp.data.entity.Job
import com.example.jobfinderapp.data.entity.JobUIModel
import com.example.jobfinderapp.entity.Company
import com.example.jobfinderapp.entity.JobSortType
import com.example.jobfinderapp.entity.Location
import com.example.jobfinderapp.entity.Result
import com.example.jobfinderapp.utils.AppLogger
import com.example.jobfinderapp.utils.AppPrefs
import com.example.jobfinderapp.utils.NetworkMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeFragViewModel @Inject constructor(private val interActor: InterActor, private val networkMonitor: NetworkMonitor, private val appPrefs: AppPrefs) : ViewModel() {

    //DB
    private val _jobsUIModel = MediatorLiveData<List<JobUIModel>>()
    val jobsUIModel: LiveData<List<JobUIModel>> = _jobsUIModel

    private var currentJobsSource: LiveData<List<JobUIModel>>? = null

    private var wasOffline = false

    //текущий фильтр
    private val _filter = MutableLiveData<JobFilter>()
    val filter: LiveData<JobFilter> = _filter

    //состояние интернета
    private val _internetState = MutableLiveData<Boolean>()
    val internetState: LiveData<Boolean> = _internetState

    //переменные для подписки на изменения выбора страны из настроек через sharedPref
    private val selectedCountryLiveData by lazy { appPrefs.observeSelectedCountry() }
    private val selectedCountryObserver = Observer<String> { newCountryCode ->
        val currentFilter = _filter.value ?: createDefaultFilter()

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

    //Создаем liveData от другого liveData, посредством map, в сравнение не берем такие параметры как, locations и searchKeyWords
    val filterState = _filter.map { current ->
        val default = createDefaultFilter()

        val isBaseChanged = current.copy(locations = default.locations, searchKeyWords = default.searchKeyWords) != default

        val hasUserLocations = (current.locations?.size ?: 0) > 1

        isBaseChanged || hasUserLocations
    }

    private val _hasPendingFilterChanges = MutableLiveData(false)
    val hasPendingFilterChanges: LiveData<Boolean> = _hasPendingFilterChanges

    //State for pagination
    var isLoading = false
    var isLastPage = false
    var currentPage = 1
    
    private val PAGE_SIZE = 10

    private val _filterBadgeCount = MediatorLiveData<Int>()
    val filterBadgeCount: LiveData<Int> = _filterBadgeCount

    //Чтобы показать, сколько фильтров установлено, только после того, как филтр был применен. То есть нажата кнопка search
    private val _isFilterInstalled = MutableLiveData<Boolean>(false)
    val isFilterInstalled: LiveData<Boolean> = _isFilterInstalled

    private var lastAppliedFilter: JobFilter? = null

    private val networkObserver = Observer<Boolean> { connected ->
        if (!connected) {
            _internetState.value = false
            wasOffline = true
        }
        if (connected && wasOffline) {
            _internetState.value = true
            wasOffline = false
            loadNextPage()
        }
    }

    init {
        observeJobsFromDb(JobSortType.DEFAULT)

        val initialFilter = createDefaultFilter()
        _filter.value = initialFilter
        loadFilteredJobList(initialFilter)

        _filterBadgeCount.addSource(_isFilterInstalled) { updateBadge() }
        _filterBadgeCount.addSource(filter) { updateBadge() }

        networkMonitor.isConnected.observeForever(networkObserver)
        selectedCountryLiveData.observeForever(selectedCountryObserver)
    }

    private fun observeJobsFromDb(sortType: JobSortType) {
        currentJobsSource?.let { _jobsUIModel.removeSource(it) }

        val newSource = when (sortType) {
            JobSortType.DEFAULT -> interActor.getJobsUIModelDB()
            JobSortType.SALARY_ASC -> interActor.getJobsBySalaryAscDB()
            JobSortType.SALARY_DESC -> interActor.getJobsBySalaryDescDB()
        }

        currentJobsSource = newSource
        _jobsUIModel.addSource(newSource) { jobs ->
            _jobsUIModel.value = jobs
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
        val filter = _filter.value ?: return

        loadFilteredJobList(filter)
        observeJobsFromDb(filter.sortBy)

        _hasPendingFilterChanges.value = false
    }


    fun installFilter(isInstalled: Boolean) {
        _isFilterInstalled.value = isInstalled
    }

    private fun updateBadge() {
        val count = getSelectedFilterCount()
        val installed = _isFilterInstalled.value ?: false

        _filterBadgeCount.value = if (installed) count else 0
    }

    fun toggleSaved(job: Job) {
        viewModelScope.launch {
            interActor.toggleSaved(job)
        }
    }

    fun onFullTimeChecked(checked: Boolean) {
        val curr = _filter.value ?: return
        _filter.value = curr.copy(onlyFullTime = checked)
        _hasPendingFilterChanges.value = true
    }

    fun onPartTimeChecked(checked: Boolean) {
        val curr = _filter.value ?: return
        _filter.value = curr.copy(onlyPartTime = checked)
        _hasPendingFilterChanges.value = true
    }

    fun onContractChecked(checked: Boolean) {
        val curr = _filter.value ?: return
        _filter.value = curr.copy(onlyContractJobs = checked)
        _hasPendingFilterChanges.value = true
    }

    fun onPermanentChecked(checked: Boolean) {
        val curr = _filter.value ?: return
        _filter.value = curr.copy(onlyPermanentJobs = checked)
        _hasPendingFilterChanges.value = true
    }

    fun onCategoryChecked(category: Category) {
        val curr = _filter.value ?: return
        _filter.value = curr.copy(category = category)
        _hasPendingFilterChanges.value = true
    }

    fun onLocationChecked(locations: List<String>) {
        val curr = _filter.value ?: return

        if (curr.locations == locations) return

        _filter.value = curr.copy(locations = locations)
        _hasPendingFilterChanges.value = true
    }

    fun onCountryChanged(country: Country) {
        val curr = _filter.value ?: return

        if (curr.country.code == country.code) return

        _filter.value = curr.copy(country = country)
        _hasPendingFilterChanges.value = true
    }

    fun onSortByChecked(sortType: JobSortType) {
        val curr = _filter.value ?: return

        _filter.value = curr.copy(sortBy = sortType)
        _hasPendingFilterChanges.value = true
    }


    fun onSearchKeyWordsChanged(words: String?) {
        val curr = _filter.value ?: return

        if (words == null) {
            val newFilter = curr.copy(searchKeyWords = words)
            _filter.value = newFilter

            loadFilteredJobList(newFilter)
        }

        if (curr.searchKeyWords != words) {
            val newFilter = curr.copy(searchKeyWords = words)
            _filter.value = newFilter

            loadFilteredJobList(newFilter)
        }
    }

    fun getSelectedFilterCount(): Int {
        val currFilter = _filter.value ?: return 0
        val defFiler = createDefaultFilter()

        var resCount = 0

        if (currFilter.onlyFullTime != defFiler.onlyFullTime) resCount++
        if (currFilter.onlyPartTime != defFiler.onlyPartTime) resCount++
        if (currFilter.onlyContractJobs != defFiler.onlyContractJobs) resCount++
        if (currFilter.onlyPermanentJobs != defFiler.onlyPermanentJobs) resCount++
        if (currFilter.category != defFiler.category) resCount++
        if (!currFilter.locations.isNullOrEmpty() && currFilter.locations.size > 1 ) resCount++
        if (currFilter.country != defFiler.country) resCount++
        if (currFilter.sortBy != defFiler.sortBy) resCount++
        if (currFilter.sortDirection != defFiler.sortDirection) resCount++

        return resCount
    }

    fun resetFilter() {
        _filter.value = createDefaultFilter()
        lastAppliedFilter = createDefaultFilter()
        loadFilteredJobList(lastAppliedFilter ?: createDefaultFilter())

        _hasPendingFilterChanges.value = false
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


    override fun onCleared() {
        super.onCleared()
        networkMonitor.isConnected.removeObserver(networkObserver)
        selectedCountryLiveData.removeObserver(selectedCountryObserver)
    }

}