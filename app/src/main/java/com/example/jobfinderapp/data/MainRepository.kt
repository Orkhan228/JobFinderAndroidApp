package com.example.jobfinderapp.data

import androidx.lifecycle.LiveData
import com.example.jobfinderapp.utils.ApiConst
import com.example.jobfinderapp.utils.CountryCode
import com.example.jobfinderapp.entity.JobFilter
import com.example.jobfinderapp.data.network.RetrofitService
import com.example.jobfinderapp.data.dao.JobDao
import com.example.jobfinderapp.data.entity.Job
import com.example.jobfinderapp.data.entity.JobWithSaved
import com.example.jobfinderapp.data.entity.SavedJob
import com.example.jobfinderapp.entity.JobDTO
import retrofit2.Response
import javax.inject.Inject

class MainRepository @Inject constructor(private val api: RetrofitService, private val jobDao: JobDao) : AppRepository {

    override suspend fun getJobsFromApi(): Response<JobDTO> =
        api.getGeneralList(CountryCode.GREAT_BRITAIN.code, 1, ApiConst.APP_ID, ApiConst.API_KEY)

    override suspend fun getFilteredJobsFromApi(jobFilter: JobFilter, page: Int): Response<JobDTO> =
        api.getFilteredList(
            countryCode = jobFilter.country.code,
            page = page,
            appId = ApiConst.APP_ID,
            apiKey = ApiConst.API_KEY,
            searchKeyWords = jobFilter.searchKeyWords,
            categoryTag = jobFilter.category?.tag,
            sortDirection = jobFilter.sortDirection,
            sortBy = if (jobFilter.sortBy) "salary" else null,
            onlyFullTime = if(jobFilter.onlyFullTime) "1" else null,
            onlyPartTime = if (jobFilter.onlyPartTime) "1" else null,
            onlyContractJobs = if (jobFilter.onlyContractJobs) "1" else null,
            onlyPermanentJobs = if (jobFilter.onlyPermanentJobs) "1" else null,
            location0 = jobFilter.locations?.getOrNull(0),
            location1 = jobFilter.locations?.getOrNull(1),
            location2 = jobFilter.locations?.getOrNull(2),
            location3 = jobFilter.locations?.getOrNull(3),
            location4 = jobFilter.locations?.getOrNull(4),
            location5 = jobFilter.locations?.getOrNull(5),
        )


    override val jobsWithSaved = jobDao.jobsWithSaved()
    override val savedJobs = jobDao.getSavedJobs()


    override suspend fun toggleSaved(job: Job) {
        if (jobDao.isInSaved(job.id)) {
            jobDao.deleteFromSaved(job.id)
        } else {
            jobDao.insertToSaved(SavedJob(job))
        }
    }

    override suspend fun refreshJobs(jobs: List<Job>) {
        jobDao.insertJobs(jobs)
    }

    override suspend fun clearDB() {
        jobDao.clearDB()
    }

    override suspend fun clearAndInsertJobsDB(jobs: List<Job>) {
        jobDao.clearAndInsertJobs(jobs)
    }

    override fun getJobById(id: String): LiveData<JobWithSaved?> {
        return jobDao.getJobById(id)
    }

}