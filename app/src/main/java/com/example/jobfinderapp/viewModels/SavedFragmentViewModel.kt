package com.example.jobfinderapp.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.jobfinderapp.App
import com.example.jobfinderapp.data.entity.Job
import com.example.jobfinderapp.data.entity.JobWithSaved
import com.example.jobfinderapp.domain.InterActor
import kotlinx.coroutines.launch
import javax.inject.Inject

class SavedFragmentViewModel : ViewModel() {

    @Inject
    lateinit var interActor: InterActor

    init {
        App.instance.appComponent.inject(this)
    }

    private val savedJobs = interActor.getOnlySavedJobsFromDB()
    val savedJobsUI = savedJobs.map { list ->
        list.map {
            JobWithSaved(it.savedJob, true)
        }
    }

    fun toggleSaved(job: Job) {
        viewModelScope.launch {
            interActor.toggleSaved(job)
        }
    }

}