package com.example.jobfinderapp.di.modules

import android.content.Context
import androidx.room.Room
import com.example.jobfinderapp.data.dao.JobDao
import com.example.jobfinderapp.data.db.JobDatabase
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
interface DatabaseModule {

    companion object {
        @Provides
        @Singleton
        fun providesJobDatabase(appContext: Context): JobDatabase =
            Room.databaseBuilder(context = appContext, klass = JobDatabase::class.java, name = "job_database")
                .fallbackToDestructiveMigration()
                .build()

        @Provides
        fun providesJobDao(jobDatabase: JobDatabase): JobDao = jobDatabase.jobDao()
    }

}