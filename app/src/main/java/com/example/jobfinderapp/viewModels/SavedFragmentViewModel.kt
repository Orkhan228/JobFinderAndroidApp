package com.example.jobfinderapp.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jobfinderapp.data.entity.Job
import com.example.jobfinderapp.domain.InterActor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SavedFragmentViewModel @Inject constructor(private val interActor: InterActor) : ViewModel() {

    val savedJobs = interActor.getOnlySavedJobsFromDB()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    fun toggleSaved(job: Job) {
        viewModelScope.launch {
            interActor.toggleSaved(job)
        }
    }
}