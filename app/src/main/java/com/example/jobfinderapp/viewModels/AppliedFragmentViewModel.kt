package com.example.jobfinderapp.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jobfinderapp.data.entity.AppliedJob
import com.example.jobfinderapp.data.entity.JobUIModel
import com.example.jobfinderapp.domain.InterActor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class AppliedFragmentViewModel @Inject constructor(private val interActor: InterActor) : ViewModel() {

    private val _pendingJobs = MutableLiveData<JobUIModel>()
    val pendingJobs: LiveData<JobUIModel> = _pendingJobs

    val appliedJobs by lazy { interActor.getOnlyAppliedJobsFromDB() }

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