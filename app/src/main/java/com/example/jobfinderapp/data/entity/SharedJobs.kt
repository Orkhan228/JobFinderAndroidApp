package com.example.jobfinderapp.data.entity

import androidx.room.Embedded
import androidx.room.Entity

@Entity(tableName = "shared_jobs_table", primaryKeys = ["id"])
data class SharedJobs(
    @Embedded val jobUIModel: JobUIModel,
)