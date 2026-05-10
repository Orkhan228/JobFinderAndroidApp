package com.example.jobfinderapp.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jobfinderapp.data.entity.Job
import com.example.jobfinderapp.domain.InterActor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SavedFragmentViewModel @Inject constructor(private val interActor: InterActor) : ViewModel() {

    private val savedJobs = interActor.getOnlySavedJobsFromDB()
    val savedJobsUI = savedJobs

    fun toggleSaved(job: Job) {
        viewModelScope.launch {
            interActor.toggleSaved(job)
        }
    }
}