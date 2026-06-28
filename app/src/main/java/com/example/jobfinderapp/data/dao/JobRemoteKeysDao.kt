package com.example.jobfinderapp.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.jobfinderapp.data.entity.JobRemoteKeysEntity

@Dao
interface JobRemoteKeysDao {
    @Upsert
    suspend fun insertRemoteKeys(remoteKeys: List<JobRemoteKeysEntity>)

    @Query("delete from job_remote_keys_table")
    suspend fun clearAllRemoteKeys()

    @Query("select * from job_remote_keys_table jr where jr.job_id = :jobId")
    suspend fun getRemoteKeysByJobID(jobId: String): JobRemoteKeysEntity?
}