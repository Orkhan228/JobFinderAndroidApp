package com.example.jobfinderapp

import android.app.Application
import com.example.jobfinderapp.di.AppComponent
import com.example.jobfinderapp.di.DaggerAppComponent
import com.example.jobfinderapp.views.notifications.NotificationHelper

class App : Application() {

    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()

        appComponent = DaggerAppComponent.factory().create(appContext = applicationContext)
        NotificationHelper.createChannel(this)
        instance = this
    }

    companion object {
        lateinit var instance: App
            private set
    }

}
