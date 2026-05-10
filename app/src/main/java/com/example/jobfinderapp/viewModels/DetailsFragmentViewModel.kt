package com.example.jobfinderapp.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailsFragmentViewModel @Inject constructor(private val interActor: InterActor) : ViewModel() {

    private val _jobsContainer = MutableLiveData<JobUIModel>()
    val jobsContainer: LiveData<JobUIModel> = _jobsContainer

    private val _applyState = MutableLiveData<Boolean>()
    val applyState: LiveData<Boolean> = _applyState


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

    fun getRemindersByJobId(jobId: String): LiveData<List<ReminderEntity>> =
        interActor.getFromRemindersByJobID(jobId)

    fun getSharedJobByJobIdOnce(jobId: String) {
        viewModelScope.launch {
            val job = interActor.getSharedJobByIdOnce(jobId)

            if (job != null) insertToJobsContainer(job)
            else AppLogger.e("SharedJobIDNull", "The job is not in shared table")
        }
    }
}