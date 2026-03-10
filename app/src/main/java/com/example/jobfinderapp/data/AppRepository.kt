package com.example.jobfinderapp.data

import androidx.lifecycle.LiveData
import com.example.jobfinderapp.entity.JobFilter
import com.example.jobfinderapp.data.entity.Job
import com.example.jobfinderapp.data.entity.JobWithSaved
import com.example.jobfinderapp.data.entity.SavedJob
import com.example.jobfinderapp.entity.JobDTO
import retrofit2.Response

interface AppRepository {

    suspend fun getJobsFromApi(): Response<JobDTO>

    suspend fun getFilteredJobsFromApi(
        jobFilter: JobFilter,
        page: Int
    ): Response<JobDTO>

    suspend fun toggleSaved(job: Job)

    suspend fun refreshJobs(jobs: List<Job>)

    suspend fun clearDB()

    suspend fun clearAndInsertJobsDB(jobs: List<Job>)

    fun getJobById(id: String): LiveData<JobWithSaved?>

    val jobsWithSaved: LiveData<List<JobWithSaved>>

    val savedJobs: LiveData<List<SavedJob>>

}