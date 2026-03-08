package com.example.jobfinderapp.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jobfinderapp.App
import com.example.jobfinderapp.data.entity.Job
import com.example.jobfinderapp.domain.InterActor
import kotlinx.coroutines.launch
import javax.inject.Inject

class DetailsFragmentViewModel : ViewModel() {

    @Inject
    lateinit var interActor: InterActor

    init {
        App.instance.appComponent.inject(this)

    }

    fun getJobById(id: String) =
        interActor.getJobById(id)

    fun toggleSaved(job: Job) {
        viewModelScope.launch {
            interActor.toggleSaved(job)
        }
    }

}