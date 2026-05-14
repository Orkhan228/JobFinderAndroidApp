package com.example.jobfinderapp.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jobfinderapp.data.entity.ReminderEntity
import com.example.jobfinderapp.data.entity.SharedJobs
import com.example.jobfinderapp.domain.InterActor
import com.example.jobfinderapp.utils.AppLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationsFragmentViewModel @Inject constructor(private val interActor: InterActor) : ViewModel() {

    val allRemindersFlow = interActor.getFromRemindersAll()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    fun deleteReminder(reminder: ReminderEntity) {
        viewModelScope.launch {
            interActor.deleteFromReminders(reminder)
            val reminders = interActor.getRemindersListByJobIdOnce(reminder.jobID)
            if (reminders.isEmpty()) {
                deleteFromSharedTableInner(reminder.jobID)
            }
        }
    }

    fun updateReminder(reminder: ReminderEntity) {
        viewModelScope.launch {
            interActor.updateReminderInTable(reminder)
        }
    }

    private suspend fun deleteFromSharedTableInner(jobId: String) {
        val job = interActor.getSharedJobByIdOnce(jobId)
        if (job != null) interActor.deleteFromSharedTable(SharedJobs(job))
        else AppLogger.e("SharedJobIDNull", "The job is not in shared table")
    }
}