package com.example.jobfinderapp.di.modules

import android.content.Context
import android.content.SharedPreferences
import com.example.jobfinderapp.data.AppRepository
import com.example.jobfinderapp.data.MainRepository
import com.example.jobfinderapp.receivers.BootReceiver
import com.example.jobfinderapp.utils.AlarmScheduler
import com.example.jobfinderapp.utils.AlarmSchedulerImpl
import com.example.jobfinderapp.utils.AppPrefs
import com.example.jobfinderapp.utils.AppPrefsImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import javax.inject.Singleton

@Module
interface RepositoryModule {

    @Binds
    fun bindRepository(repoImpl: MainRepository): AppRepository

    @Binds
    fun bindAlarmScheduler(impl: AlarmSchedulerImpl): AlarmScheduler

    @Binds
    @Singleton
    fun bindAppPrefs(impl: AppPrefsImpl): AppPrefs

    companion object {
        @Provides
        @Singleton
        fun provideSharedPref(context: Context): SharedPreferences =
            context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    }

}