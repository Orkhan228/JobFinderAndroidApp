package com.example.jobfinderapp.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.jobfinderapp.data.entity.AppliedJob
import com.example.jobfinderapp.data.entity.Job
import com.example.jobfinderapp.data.entity.JobUIModel
import com.example.jobfinderapp.data.entity.ReminderEntity
import com.example.jobfinderapp.data.entity.SavedJob
import com.example.jobfinderapp.data.entity.SharedJobs

@Dao
interface JobDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJobs(jobList: List<Job>)

    @Query("DELETE FROM job_table")
    suspend fun clearDB()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertToSaved(savedJob: SavedJob)

    @Query("DELETE FROM saved_jobs WHERE id = :id")
    suspend fun deleteFromSaved(id: String)

    @Query("SELECT EXISTS(SELECT 1 FROM saved_jobs WHERE id = :id)")
    suspend fun isInSaved(id: String): Boolean

    //Sorted queries
    @Query("""
    SELECT job_table.*,
    case when saved_jobs.id is not null then 1 else 0 end as isSaved,
    case when applied_jobs.id is not null then 1 else 0 end as isApplied
    from job_table
    left join saved_jobs on saved_jobs.id = job_table.id
    left join applied_jobs on applied_jobs.id = job_table.id
    """)
    fun getAllJobs(): LiveData<List<JobUIModel>>

    @Query("""
    SELECT job_table.*,
    case when saved_jobs.id is not null then 1 else 0 end as isSaved,
    case when applied_jobs.id is not null then 1 else 0 end as isApplied
    from job_table 
    left join saved_jobs on saved_jobs.id = job_table.id
    left join applied_jobs on applied_jobs.id = job_table.id
    ORDER BY COALESCE(job_table.required_salary, 0) ASC
    """)
    fun getJobsBySalaryAsc(): LiveData<List<JobUIModel>>

    @Query("""
    SELECT job_table.*,
    case when saved_jobs.id is not null then 1 else 0 end as isSaved,
    case when applied_jobs.id is not null then 1 else 0 end as isApplied
    from job_table 
    left join saved_jobs on saved_jobs.id = job_table.id
    left join applied_jobs on applied_jobs.id = job_table.id
    ORDER BY COALESCE(job_table.required_salary, 0) DESC
    """)
    fun getJobsBySalaryDesc(): LiveData<List<JobUIModel>>

    @Query("""
    select 
    saved_jobs.*,
    1 as isSaved,
    case when applied_jobs.id is not null then 1 else 0 end as isApplied
    from saved_jobs
    left join applied_jobs on saved_jobs.id = applied_jobs.id
    """)
    fun getSavedJobs(): LiveData<List<JobUIModel>>

    @Query("SELECT job_table.*, case when saved_jobs.id is not null then 1 else 0 end as isSaved,  case when applied_jobs.id is not null then 1 else 0 end as isApplied from job_table left join saved_jobs on saved_jobs.id = job_table.id left join applied_jobs on applied_jobs.id = job_table.id where job_table.id = :id")
    fun getJobById(id: String): LiveData<JobUIModel?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertToApplied(appliedJob: AppliedJob)

    @Query("DELETE FROM applied_jobs where id = :id")
    suspend fun deleteFromApplied(id: String)

    @Query("select exists(select 1 from applied_jobs where id = :id)")
    suspend fun isInApplied(id: String): Boolean

    @Query("""
        select applied_jobs.*,
        case when saved_jobs.id is not null then 1 else 0 end as isSaved,
        1 as isApplied,
        applied_jobs.applied_time as appliedTime
        from applied_jobs
        left join saved_jobs on saved_jobs.id = applied_jobs.id
    """)
    fun getAppliedJobs(): LiveData<List<JobUIModel>>

    @Transaction
    suspend fun clearAndInsertJobs(jobList: List<Job>) {
        clearDB()
        insertJobs(jobList)
    }


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertToSharedJobsTable(sharedJobs: SharedJobs)

    @Delete
    suspend fun deleteFromSharedTable(sharedJobs: SharedJobs)

    @Query("select shared_jobs_table.* from shared_jobs_table where id = :sharedId")
    fun getSharedJobById(sharedId: String): LiveData<JobUIModel>

    @Query("select shared_jobs_table.* from shared_jobs_table where id = :sharedId limit 1")
    suspend fun getSharedJobByIdOnce(sharedId: String): JobUIModel?





    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertToReminderTable(reminderEntity: ReminderEntity): Long

    @Delete
    suspend fun deleteFromReminderTable(reminderEntity: ReminderEntity)

    @Update
    suspend fun updateReminderInTable(reminderEntity: ReminderEntity)

    @Query("select reminder_table.* from reminder_table")
    fun getFromReminderTable(): LiveData<List<ReminderEntity>>

    @Query("select reminder_table.* from reminder_table where job_id = :jobId order by trigger_time asc")
    fun getFromReminderTableByJobID(jobId: String): LiveData<List<ReminderEntity>>

    @Query("select reminder_table.* from reminder_table order by trigger_time asc")
    suspend fun getRemindersListByOnce(): List<ReminderEntity>

    @Query("select reminder_table.* from reminder_table where job_id = :jobId limit 1")
    suspend fun getRemindersListByJobIdOnce(jobId: String): List<ReminderEntity>
}