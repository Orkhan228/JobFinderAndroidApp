package com.example.jobfinderapp.data.entity

import androidx.room.Embedded
import androidx.room.Entity

@Entity(tableName = "saved_jobs", primaryKeys = ["id"])
data class SavedJob(
    @Embedded val savedJob: Job
)