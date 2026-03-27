package com.example.jobfinderapp.data

import androidx.lifecycle.LiveData
import com.example.jobfinderapp.data.entity.AppliedJob
import com.example.jobfinderapp.entity.JobFilter
import com.example.jobfinderapp.data.entity.Job
import com.example.jobfinderapp.data.entity.JobUIModel
import com.example.jobfinderapp.data.entity.SavedJob
import com.example.jobfinderapp.data.entity.SharedJobs
import com.example.jobfinderapp.entity.JobDTO
import retrofit2.Response

interface AppRepository {

    suspend fun getJobsFromApi(): Response<JobDTO>

    suspend fun getFilteredJobsFromApi(
        jobFilter: JobFilter,
        page: Int
    ): Response<JobDTO>

    suspend fun toggleSaved(job: Job)

    suspend fun toggleApplied(appliedJob: AppliedJob)

    suspend fun refreshJobs(jobs: List<Job>)

    suspend fun clearDB()

    suspend fun clearAndInsertJobsDB(jobs: List<Job>)

    suspend fun deleteFromApplied(jobID: String)

    fun getJobById(id: String): LiveData<JobUIModel?>

    suspend fun insertToSharedJobsTable(sharedJobs: SharedJobs)

    fun getSharedJob(sharedId: String): LiveData<JobUIModel>

    val jobsUIModel: LiveData<List<JobUIModel>>

    val savedJobs: LiveData<List<JobUIModel>>

    val appliedJobs: LiveData<List<JobUIModel>>



}