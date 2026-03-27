package com.example.jobfinderapp.data.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity

@Entity(tableName = "applied_jobs", primaryKeys = ["id"])
data class AppliedJob(
    @Embedded val job: Job,
    @ColumnInfo(name = "applied_time") val appliedTime: Long
)