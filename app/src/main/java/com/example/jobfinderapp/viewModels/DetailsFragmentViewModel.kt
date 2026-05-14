package com.example.jobfinderapp.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jobfinderapp.data.entity.AppliedJob
import com.example.jobfinderapp.data.entity.Job
import com.example.jobfinderapp.data.entity.JobUIModel
import com.example.jobfinderapp.data.entity.ReminderEntity
import com.example.jobfinderapp.data.entity.SharedJobs
import com.example.jobfinderapp.domain.InterActor
import com.example.jobfinderapp.utils.AppLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailsFragmentViewModel @Inject constructor(private val interActor: InterActor) : ViewModel() {

    private val _jobsContainer = MutableStateFlow<JobUIModel?>(null)
    val jobsContainer = _jobsContainer.asStateFlow()

    private val _applyState = MutableSharedFlow<Unit>()
    val applyState = _applyState.asSharedFlow()

    private val jobIdFlow = MutableStateFlow<String?>(null)
    @OptIn(ExperimentalCoroutinesApi::class)
    val reminderFlow = jobIdFlow
        .filterNotNull()
        .flatMapLatest { jobId ->
            interActor.getFromRemindersByJobID(jobId)
        }
        .stateIn(viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList())


    fun toggleSaved(job: Job) {

        _jobsContainer.update { current ->
            current?.copy(
                isSaved = !current.isSaved
            )
        }

        viewModelScope.launch {
            interActor.toggleSaved(job)
        }
    }

    fun toggleApplied(toggleAt: Long) {

        val current = _jobsContainer.value ?: return

        val updated = current.copy(
            isApplied = !current.isApplied
        )

        _jobsContainer.update {
            updated
        }

        viewModelScope.launch {
            interActor.toggleApplied(
                AppliedJob(updated.job, toggleAt)
            )

            _applyState.emit(Unit)
        }
    }

    fun insertToJobsContainer(jobUIModel: JobUIModel) {
        _jobsContainer.update {
            jobUIModel
        }
    }

    fun insertSharedJob() {
        val currentJob = _jobsContainer.value ?: return

        viewModelScope.launch {
            interActor.insertToSharedJobsTable(SharedJobs(currentJob))
        }
    }

    private suspend fun deleteFromSharedJobInner() {
        val currentJob = _jobsContainer.value ?: return
        interActor.deleteFromSharedTable(SharedJobs(currentJob))
    }

    private suspend fun insertToSharedJobInner() {
        val currentJob = _jobsContainer.value ?: return
        interActor.insertToSharedJobsTable(SharedJobs(currentJob))
    }

    fun insertToReminders(reminderEntity: ReminderEntity) {
        viewModelScope.launch {
            val remindersList = interActor.getRemindersListByJobIdOnce(reminderEntity.jobID)
            interActor.insertToReminders(reminderEntity)
            if (remindersList.isEmpty()) {
                insertToSharedJobInner()
            }
        }
    }

    fun deleteFromReminders(reminderEntity: ReminderEntity) {
        viewModelScope.launch {
            interActor.deleteFromReminders(reminderEntity)
            val remindersList = interActor.getRemindersListByJobIdOnce(reminderEntity.jobID)
            if (remindersList.isEmpty()) {
                deleteFromSharedJobInner()
            }
        }
    }

    fun updateReminderInTable(reminderEntity: ReminderEntity) {
        viewModelScope.launch {
            interActor.updateReminderInTable(reminderEntity)
        }
    }

    fun updateJobId(jobId: String) {
        jobIdFlow.update {
            jobId
        }
    }

    fun getSharedJobByJobIdOnce(jobId: String) {
        viewModelScope.launch {
            val job = interActor.getSharedJobByIdOnce(jobId)

            if (job != null) insertToJobsContainer(job)
            else AppLogger.e("SharedJobIDNull", "The job is not in shared table")
        }
    }
}