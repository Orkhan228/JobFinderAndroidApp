package com.example.jobfinderapp

import android.app.Application
import com.example.jobfinderapp.di.AppComponent
import com.example.jobfinderapp.di.DaggerAppComponent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class App : Application() {

    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()

        appComponent = DaggerAppComponent.factory().create(appContext = applicationContext)
        instance = this
    }

    companion object {
        lateinit var instance: App
            private set
    }

}
