package com.example.jobfinderapp.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jobfinderapp.App
import com.example.jobfinderapp.data.entity.AppliedJob
import com.example.jobfinderapp.data.entity.Job
import com.example.jobfinderapp.data.entity.JobUIModel
import com.example.jobfinderapp.data.entity.SharedJobs
import com.example.jobfinderapp.domain.InterActor
import kotlinx.coroutines.launch
import javax.inject.Inject

class DetailsFragmentViewModel : ViewModel() {

    @Inject
    lateinit var interActor: InterActor

    private val _jobsContainer = MutableLiveData<JobUIModel>()
    val jobsContainer: LiveData<JobUIModel> = _jobsContainer

    private val _applyState = MutableLiveData<Boolean>()
    val applyState: LiveData<Boolean> = _applyState

    init {
        App.instance.appComponent.inject(this)

    }

    fun toggleSaved(job: Job) {
        val current = _jobsContainer.value ?: return

        val updated = current.copy(
            isSaved = !current.isSaved
        )

        _jobsContainer.value = updated


        viewModelScope.launch {
            interActor.toggleSaved(job)
        }
    }

    fun toggleApplied(toggleAt: Long) {

        val current = _jobsContainer.value ?: return

        val updated = current.copy(
            isApplied = !current.isApplied
        )

        _jobsContainer.value = updated

        _applyState.value = true

        viewModelScope.launch {
            interActor.toggleApplied(
                AppliedJob(updated.job, toggleAt)
            )
        }


    }

    fun insertToJobsContainer(jobUIModel: JobUIModel) {
        _jobsContainer.value = jobUIModel
    }

    fun insertSharedJob() {
        val currentJob = _jobsContainer.value ?: return

        viewModelScope.launch {
            interActor.insertToSharedJobsTable(SharedJobs(currentJob))
        }
    }

    fun getSharedJobById(sharedId: String): LiveData<JobUIModel> =
        interActor.getSharedJobById(sharedId)


}