package com.example.jobfinderapp.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminder_table")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    @ColumnInfo(name = "job_id") val jobID: String,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "message") val message: String?,
    @ColumnInfo(name = "trigger_time") val triggerAtMillis: Long,
)