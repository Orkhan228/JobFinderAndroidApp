package com.example.jobfinderapp.di

import android.content.Context
import com.example.jobfinderapp.App
import com.example.jobfinderapp.data.AppRepository
import com.example.jobfinderapp.di.modules.DatabaseModule
import com.example.jobfinderapp.di.modules.NetworkModule
import com.example.jobfinderapp.di.modules.RepositoryModule
import com.example.jobfinderapp.utils.AlarmScheduler
import com.example.jobfinderapp.viewModels.AppliedFragmentViewModel
import com.example.jobfinderapp.viewModels.DetailsFragmentViewModel
import com.example.jobfinderapp.viewModels.HomeFragViewModel
import com.example.jobfinderapp.viewModels.NotificationsFragmentViewModel
import com.example.jobfinderapp.viewModels.SavedFragmentViewModel
import com.example.jobfinderapp.viewModels.SettingsFragmentViewModel
import com.example.jobfinderapp.views.activities.MainActivity
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [
    NetworkModule::class,
    RepositoryModule::class,
    DatabaseModule::class,
])
interface AppComponent {

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance appContext: Context): AppComponent
    }

    fun getAppRepository(): AppRepository
    fun getAlarmScheduler(): AlarmScheduler

    fun inject(hfViewModel: HomeFragViewModel)
    fun inject(sfViewModel: SavedFragmentViewModel)
    fun inject(dfViewModel: DetailsFragmentViewModel)
    fun inject(afViewModel: AppliedFragmentViewModel)
    fun inject(notViewModel: NotificationsFragmentViewModel)
    fun inject(setViewModel: SettingsFragmentViewModel)
    fun inject(activity: MainActivity)
}