package com.example.jobfinderapp.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.jobfinderapp.utils.ApiConst
import com.example.jobfinderapp.utils.CountryCode
import com.example.jobfinderapp.entity.JobFilter
import com.example.jobfinderapp.data.network.RetrofitService
import com.example.jobfinderapp.data.dao.JobDao
import com.example.jobfinderapp.data.db.JobDatabase
import com.example.jobfinderapp.data.entity.AppliedJob
import com.example.jobfinderapp.data.entity.Job
import com.example.jobfinderapp.data.entity.JobUIModel
import com.example.jobfinderapp.data.entity.ReminderEntity
import com.example.jobfinderapp.data.entity.SavedJob
import com.example.jobfinderapp.data.entity.SharedJobs
import com.example.jobfinderapp.data.paging.JobHFRemoteMediator
import com.example.jobfinderapp.entity.JobDTO
import com.example.jobfinderapp.utils.AlarmScheduler
import com.example.jobfinderapp.utils.SettingsDataStore
import kotlinx.coroutines.flow.Flow
import retrofit2.Response
import javax.inject.Inject

class MainRepository @Inject constructor(
    private val api: RetrofitService,
    private val database: JobDatabase,
    private val jobDao: JobDao,
    private val alarmScheduler: AlarmScheduler,
) : AppRepository {

    override suspend fun getJobsFromApi(): Response<JobDTO> =
        api.getGeneralList(CountryCode.GREAT_BRITAIN.code, 1, ApiConst.APP_ID, ApiConst.API_KEY)


    override suspend fun getFilteredJobsFromApi(jobFilter: JobFilter, page: Int): Response<JobDTO> =
        api.getFilteredList(
            countryCode = jobFilter.country.code,
            page = page,
            searchKeyWords = jobFilter.searchKeyWords,
            categoryTag = jobFilter.category?.tag,
            sortDirection = jobFilter.sortDirection,
            sortBy = jobFilter.sortBy,
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


    //MAIN table
    @OptIn(ExperimentalPagingApi::class)
    override fun getJobsUIModelDB(filter: JobFilter): Flow<PagingData<JobUIModel>> =
        Pager(
            config = PagingConfig(
                pageSize = 10,
                initialLoadSize = 10,
                prefetchDistance = 3,
                enablePlaceholders = true
            ),
            remoteMediator = JobHFRemoteMediator(
                api = api,
                database = database,
                jobFilter = filter,
            ),
            pagingSourceFactory = {
                jobDao.getAllJobs()
            }
        ).flow

    override val savedJobs = jobDao.getSavedJobs()

    override val appliedJobs = jobDao.getAppliedJobs()

    override suspend fun toggleSaved(job: Job) {
        if (jobDao.isInSaved(job.id)) {
            jobDao.deleteFromSaved(job.id)
        } else {
            jobDao.insertToSaved(SavedJob(job))
        }
    }

    override suspend fun toggleApplied(appliedJob: AppliedJob) {
        if (jobDao.isInApplied(appliedJob.job.id)) {
            jobDao.deleteFromApplied(appliedJob.job.id)
        } else {
            jobDao.insertToApplied(appliedJob)
        }
    }

    override suspend fun deleteFromApplied(jobID: String) {
        jobDao.deleteFromApplied(jobID)
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

    override fun getJobById(id: String): Flow<JobUIModel?> {
        return jobDao.getJobById(id)
    }



    //SHARED table
    override suspend fun insertToSharedJobsTable(sharedJobs: SharedJobs) {
        jobDao.insertToSharedJobsTable(sharedJobs)
    }

    override suspend fun getSharedJobByIdOnce(sharedId: String): JobUIModel? =
        jobDao.getSharedJobByIdOnce(sharedId)


    override suspend fun deleteFromSharedTable(sharedJobs: SharedJobs) {
        jobDao.deleteFromSharedTable(sharedJobs)
    }

    override fun getSharedJob(sharedId: String): Flow<JobUIModel> =
        jobDao.getSharedJobById(sharedId)




    //REMINDERS table
    override suspend fun insertToReminders(reminderEntity: ReminderEntity): Long {
        val id = jobDao.insertToReminderTable(reminderEntity)
        val savedReminder = reminderEntity.copy(id = id)
        alarmScheduler.schedule(savedReminder)
        return id
    }

    override suspend fun deleteFromReminders(reminderEntity: ReminderEntity) {
        alarmScheduler.cancel(reminderEntity)
        jobDao.deleteFromReminderTable(reminderEntity)
    }

    override suspend fun updateReminderInTable(reminderEntity: ReminderEntity) {
        alarmScheduler.cancel(reminderEntity)
        jobDao.updateReminderInTable(reminderEntity)
        alarmScheduler.schedule(reminderEntity)
    }

    override suspend fun getRemindersListByOnce(): List<ReminderEntity> =
        jobDao.getRemindersListByOnce()

    override suspend fun getRemindersListByJobIdOnce(jobId: String): List<ReminderEntity> =
        jobDao.getRemindersListByJobIdOnce(jobId)

    override fun getFromRemindersAll(): Flow<List<ReminderEntity>> {
        return jobDao.getFromReminderTable()
    }

    override fun getFromRemindersByJobID(jobID: String): Flow<List<ReminderEntity>> {
        return jobDao.getFromReminderTableByJobID(jobID)
    }
}