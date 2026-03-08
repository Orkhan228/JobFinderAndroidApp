package com.example.jobfinderapp.di.modules

import com.example.jobfinderapp.data.AppRepository
import com.example.jobfinderapp.data.MainRepository
import dagger.Binds
import dagger.Module

@Module
interface RepositoryModule {

    @Binds
    fun bindRepository(repoImpl: MainRepository): AppRepository
}