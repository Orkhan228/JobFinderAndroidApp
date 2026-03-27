package com.example.jobfinderapp.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jobfinderapp.App
import com.example.jobfinderapp.data.entity.AppliedJob
import com.example.jobfinderapp.data.entity.JobUIModel
import com.example.jobfinderapp.domain.InterActor
import kotlinx.coroutines.launch
import javax.inject.Inject

class AppliedFragmentViewModel : ViewModel() {

    @Inject
    lateinit var interActor: InterActor


    private val _pendingJobs = MutableLiveData<JobUIModel>()
    val pendingJobs: LiveData<JobUIModel> = _pendingJobs

    val appliedJobs by lazy { interActor.getOnlyAppliedJobsFromDB() }

    init {
        App.instance.appComponent.inject(this)

    }

    fun savePendingJob(jobUIModel: JobUIModel) {
        _pendingJobs.value = jobUIModel
    }

    fun confirmWithdraw() {

        val jobUIModel = _pendingJobs.value ?: return

        viewModelScope.launch {
            interActor.deleteFromApplied(jobUIModel.job.id)
        }
    }

    fun toggleApplied(jobUIModel: JobUIModel) {
        viewModelScope.launch {
            interActor.toggleApplied(AppliedJob(jobUIModel.job, jobUIModel.appliedTime ?: 0))
        }
    }

}