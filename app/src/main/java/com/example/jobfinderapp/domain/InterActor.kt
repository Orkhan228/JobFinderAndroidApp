package com.example.jobfinderapp.domain

import androidx.lifecycle.LiveData
import com.example.jobfinderapp.entity.JobFilter
import com.example.jobfinderapp.data.AppRepository
import com.example.jobfinderapp.data.entity.AppliedJob
import com.example.jobfinderapp.data.entity.Job
import com.example.jobfinderapp.data.entity.JobUIModel
import com.example.jobfinderapp.data.entity.ReminderEntity
import com.example.jobfinderapp.data.entity.SharedJobs
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
    fun getJobsUIModelDB(): LiveData<List<JobUIModel>> = repo.getJobsUIModelDB()
    fun getJobsBySalaryAscDB(): LiveData<List<JobUIModel>> = repo.getJobsBySalaryAscDB()
    fun getJobsBySalaryDescDB(): LiveData<List<JobUIModel>> = repo.getJobsBySalaryDescDB()

    fun getOnlySavedJobsFromDB(): LiveData<List<JobUIModel>> = repo.savedJobs

    fun getOnlyAppliedJobsFromDB(): LiveData<List<JobUIModel>> = repo.appliedJobs

    suspend fun clearAndInsertJobsDB(jobs: List<Job>) = repo.clearAndInsertJobsDB(jobs)

    suspend fun toggleSaved(job: Job) = repo.toggleSaved(job)

    suspend fun toggleApplied(appliedJob: AppliedJob) = repo.toggleApplied(appliedJob)

    suspend fun refreshJobs(jobs: List<Job>) = repo.refreshJobs(jobs)

    suspend fun deleteFromApplied(jobID: String) = repo.deleteFromApplied(jobID)

    fun getJobById(id: String) = repo.getJobById(id)

    suspend fun clearDB() = repo.clearDB()



    suspend fun insertToSharedJobsTable(sharedJobs: SharedJobs) = repo.insertToSharedJobsTable(sharedJobs)
    suspend fun deleteFromSharedTable(sharedJobs: SharedJobs) = repo.deleteFromSharedTable(sharedJobs)
    suspend fun getSharedJobByIdOnce(sharedId: String) = repo.getSharedJobByIdOnce(sharedId)
    fun getSharedJobById(sharedId: String) = repo.getSharedJob(sharedId)




    suspend fun insertToReminders(reminderEntity: ReminderEntity): Long {
        return repo.insertToReminders(reminderEntity)
    }

    suspend fun deleteFromReminders(reminderEntity: ReminderEntity) {
        repo.deleteFromReminders(reminderEntity)
    }

    suspend fun updateReminderInTable(reminderEntity: ReminderEntity) {
        repo.updateReminderInTable(reminderEntity)
    }

    suspend fun getRemindersListByJobIdOnce(jobId: String): List<ReminderEntity> =
        repo.getRemindersListByJobIdOnce(jobId)

    fun getFromRemindersAll(): LiveData<List<ReminderEntity>> {
        return repo.getFromRemindersAll()
    }

    fun getFromRemindersByJobID(jobID: String): LiveData<List<ReminderEntity>> {
        return repo.getFromRemindersByJobID(jobID)
    }
}