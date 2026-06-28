package com.example.jobfinderapp.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "job_remote_keys_table")
data class JobRemoteKeysEntity(
    @ColumnInfo(name = "job_id") @PrimaryKey val jobId: String,
    @ColumnInfo(name = "next_key") val nextKey: Int?,
    @ColumnInfo(name = "prev_key") val prevKey: Int?
)

