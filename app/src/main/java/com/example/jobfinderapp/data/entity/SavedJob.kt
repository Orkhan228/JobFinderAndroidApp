package com.example.jobfinderapp.data.entity

import androidx.room.Embedded
import androidx.room.Entity

@Entity(tableName = "saved_jobs", primaryKeys = ["saved_id"])
data class SavedJob(
    @Embedded("saved_")
    val savedJob: Job
)