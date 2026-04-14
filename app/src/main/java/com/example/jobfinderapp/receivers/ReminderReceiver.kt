package com.example.jobfinderapp.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.jobfinderapp.views.notifications.NotificationConstants
import com.example.jobfinderapp.views.notifications.NotificationHelper

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.

        val reminderId = intent.extras?.getLong(NotificationConstants.REMINDER_ID, -1)
        val jobId = intent.extras?.getString(NotificationConstants.REMINDER_JOB_ID)
        val title = intent.extras?.getString(NotificationConstants.REMINDER_TITLE)
        val message = intent.extras?.getString(NotificationConstants.REMINDER_MESSAGE)

        if (reminderId == -1L || reminderId == null || jobId == null || title.isNullOrEmpty()) {
            return
        }

        NotificationHelper.createReminderNotification(
            context,
            jobId,
            title,
            message,
            reminderId.toInt()
        )
    }
}