package com.example.jobfinderapp.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jobfinderapp.data.entity.JobUIModel
import com.example.jobfinderapp.domain.InterActor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class AppliedFragmentViewModel @Inject constructor(private val interActor: InterActor) : ViewModel() {

    private var pendingJob: JobUIModel? = null

    val appliedJobsFlow =
        interActor.getOnlyAppliedJobsFromDB()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    fun savePendingJob(jobUIModel: JobUIModel) {
        pendingJob = jobUIModel
    }

    fun confirmWithdraw() {
        val jobUIModel = pendingJob ?: return

        viewModelScope.launch {
            interActor.deleteFromApplied(jobUIModel.job.id)
        }
    }

}