package com.example.jobfinderapp.di.modules

import android.content.Context
import androidx.room.Room
import com.example.jobfinderapp.data.dao.JobDao
import com.example.jobfinderapp.data.db.JobDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun providesJobDatabase(@ApplicationContext appContext: Context): JobDatabase =
        Room.databaseBuilder(
            context = appContext,
            klass = JobDatabase::class.java,
            name = "job_database"
        )
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun providesJobDao(jobDatabase: JobDatabase): JobDao = jobDatabase.jobDao()

}