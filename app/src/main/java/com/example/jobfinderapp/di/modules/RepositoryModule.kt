package com.example.jobfinderapp.di.modules

import android.content.Context
import android.content.SharedPreferences
import com.example.jobfinderapp.data.AppRepository
import com.example.jobfinderapp.data.MainRepository
import com.example.jobfinderapp.utils.AlarmScheduler
import com.example.jobfinderapp.utils.AlarmSchedulerImpl
import com.example.jobfinderapp.utils.AppPrefs
import com.example.jobfinderapp.utils.AppPrefsImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindRepository(repoImpl: MainRepository): AppRepository

    @Binds
    @Singleton
    abstract fun bindAlarmScheduler(impl: AlarmSchedulerImpl): AlarmScheduler

    @Binds
    @Singleton
    abstract fun bindAppPrefs(impl: AppPrefsImpl): AppPrefs

    companion object {
        @Provides
        @Singleton
        fun provideSharedPref(@ApplicationContext context: Context): SharedPreferences =
            context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    }

}