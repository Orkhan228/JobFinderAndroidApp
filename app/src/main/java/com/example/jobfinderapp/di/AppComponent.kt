package com.example.jobfinderapp.di

import com.example.jobfinderapp.App
import com.example.jobfinderapp.views.activities.MainActivity
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [
    NetworkModule::class
])
interface AppComponent {

    fun inject(act: MainActivity)
}