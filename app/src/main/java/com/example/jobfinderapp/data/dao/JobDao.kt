package com.example.jobfinderapp.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.jobfinderapp.data.entity.Job
import com.example.jobfinderapp.data.entity.JobWithSaved
import com.example.jobfinderapp.data.entity.SavedJob

@Dao
interface JobDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJobs(jobList: List<Job>)

    @Query("DELETE FROM job_table")
    suspend fun clearDB()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertToSaved(savedJob: SavedJob)

    @Query("DELETE FROM saved_jobs WHERE saved_id = :id")
    suspend fun deleteFromSaved(id: String)

    @Query("SELECT EXISTS(SELECT 1 FROM saved_jobs WHERE saved_id = :id)")
    suspend fun isInSaved(id: String): Boolean

    @Query("SELECT job_table.*, case when saved_jobs.saved_id is not null then 1 else 0 end as isSaved from job_table left join saved_jobs on saved_jobs.saved_id = job_table.id")
    fun jobsWithSaved(): LiveData<List<JobWithSaved>>

    @Query("SELECT * from saved_jobs")
    fun getSavedJobs(): LiveData<List<SavedJob>>

    @Query("SELECT job_table.*, case when saved_jobs.saved_id is not null then 1 else 0 end as isSaved from job_table left join saved_jobs on saved_jobs.saved_id = job_table.id where job_table.id = :id")
    fun getJobById(id: String): LiveData<JobWithSaved?>

    @Transaction
    suspend fun clearAndInsertJobs(jobList: List<Job>) {
        clearDB()
        insertJobs(jobList)
    }

}