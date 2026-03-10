package com.example.jobfinderapp.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.jobfinderapp.utils.LocationTypeConverter
import com.example.jobfinderapp.data.dao.JobDao
import com.example.jobfinderapp.data.entity.Job
import com.example.jobfinderapp.data.entity.SavedJob

@Database(entities = [Job::class, SavedJob::class], version = 4, exportSchema = false)
@TypeConverters(LocationTypeConverter::class)
abstract class JobDatabase : RoomDatabase() {
    abstract fun jobDao(): JobDao
}