package com.example.jobfinderapp.data

import androidx.paging.PagingData
import com.example.jobfinderapp.data.entity.AppliedJob
import com.example.jobfinderapp.entity.JobFilter
import com.example.jobfinderapp.data.entity.Job
import com.example.jobfinderapp.data.entity.JobUIModel
import com.example.jobfinderapp.data.entity.ReminderEntity
import com.example.jobfinderapp.data.entity.SharedJobs
import com.example.jobfinderapp.entity.JobDTO
import com.example.jobfinderapp.entity.Result
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

interface AppRepository {

    suspend fun getJobsFromApi(): Response<JobDTO>
    suspend fun getFilteredJobsFromApi(
        jobFilter: JobFilter,
        page: Int
    ): Response<JobDTO>

    //main flow
    fun getJobsUIModelDB(filter: JobFilter): Flow<PagingData<JobUIModel>>

    suspend fun toggleSaved(job: Job)
    suspend fun toggleApplied(appliedJob: AppliedJob)

    suspend fun refreshJobs(jobs: List<Job>)
    suspend fun clearDB()
    suspend fun clearAndInsertJobsDB(jobs: List<Job>)

    suspend fun deleteFromApplied(jobID: String)

    fun getJobById(id: String): Flow<JobUIModel?>

    suspend fun insertToSharedJobsTable(sharedJobs: SharedJobs)
    suspend fun deleteFromSharedTable(sharedJobs: SharedJobs)
    suspend fun getSharedJobByIdOnce(sharedId: String): JobUIModel?
    fun getSharedJob(sharedId: String): Flow<JobUIModel>

    val savedJobs: Flow<List<JobUIModel>>

    val appliedJobs: Flow<List<JobUIModel>>

    suspend fun insertToReminders(reminderEntity: ReminderEntity): Long
    suspend fun deleteFromReminders(reminderEntity: ReminderEntity)
    suspend fun updateReminderInTable(reminderEntity: ReminderEntity)
    suspend fun getRemindersListByOnce(): List<ReminderEntity>
    suspend fun getRemindersListByJobIdOnce(jobId: String): List<ReminderEntity>
    fun getFromRemindersAll(): Flow<List<ReminderEntity>>
    fun getFromRemindersByJobID(jobID: String): Flow<List<ReminderEntity>>
}