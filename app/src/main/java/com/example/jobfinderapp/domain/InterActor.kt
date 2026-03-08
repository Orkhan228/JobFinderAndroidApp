package com.example.jobfinderapp.domain

import androidx.lifecycle.LiveData
import com.example.jobfinderapp.entity.JobFilter
import com.example.jobfinderapp.data.AppRepository
import com.example.jobfinderapp.data.entity.Job
import com.example.jobfinderapp.data.entity.JobWithSaved
import com.example.jobfinderapp.data.entity.SavedJob
import com.example.jobfinderapp.entity.JobDTO
import retrofit2.Response
import javax.inject.Inject

class InterActor @Inject constructor(private val repo: AppRepository) {

    //API
    suspend fun getJobsFromRepo(): Response<JobDTO> =
        repo.getJobsFromApi()

    suspend fun getFilteredJobsFromRepo(filter: JobFilter, page: Int = 1): Response<JobDTO> =
        repo.getFilteredJobsFromApi(filter, page)


    //LOCAL DB
    fun getJobsWithSavedDB(): LiveData<List<JobWithSaved>> = repo.jobsWithSaved

    fun getOnlySavedJobsFromDB(): LiveData<List<SavedJob>> = repo.savedJobs

    suspend fun clearAndInsertJobsDB(jobs: List<Job>) = repo.clearAndInsertJobsDB(jobs)

    suspend fun toggleSaved(job: Job) = repo.toggleSaved(job)

    suspend fun refreshJobs(jobs: List<Job>) = repo.refreshJobs(jobs)

    fun getJobById(id: String) = repo.getJobById(id)

    suspend fun clearDB() = repo.clearDB()
}