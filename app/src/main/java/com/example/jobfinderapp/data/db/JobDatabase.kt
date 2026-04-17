package com.example.jobfinderapp.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.jobfinderapp.utils.LocationTypeConverter
import com.example.jobfinderapp.data.dao.JobDao
import com.example.jobfinderapp.data.entity.AppliedJob
import com.example.jobfinderapp.data.entity.Job
import com.example.jobfinderapp.data.entity.ReminderEntity
import com.example.jobfinderapp.data.entity.SavedJob
import com.example.jobfinderapp.data.entity.SharedJobs

@Database(entities = [Job::class, SavedJob::class, AppliedJob::class, SharedJobs::class, ReminderEntity::class], version = 7, exportSchema = false)
@TypeConverters(LocationTypeConverter::class)
abstract class JobDatabase : RoomDatabase() {
    abstract fun jobDao(): JobDao
}