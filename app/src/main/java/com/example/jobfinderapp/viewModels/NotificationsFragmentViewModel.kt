package com.example.jobfinderapp.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jobfinderapp.App
import com.example.jobfinderapp.data.entity.ReminderEntity
import com.example.jobfinderapp.data.entity.SharedJobs
import com.example.jobfinderapp.domain.InterActor
import com.example.jobfinderapp.utils.AppLogger
import kotlinx.coroutines.launch
import javax.inject.Inject

class NotificationsFragmentViewModel : ViewModel() {

    @Inject
    lateinit var interActor: InterActor

    init {
        App.instance.appComponent.inject(this)
    }

    fun getAllReminders(): LiveData<List<ReminderEntity>> =
        interActor.getFromRemindersAll()

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