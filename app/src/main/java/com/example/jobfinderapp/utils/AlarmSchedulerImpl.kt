package com.example.jobfinderapp.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.jobfinderapp.data.entity.ReminderEntity
import com.example.jobfinderapp.receivers.ReminderReceiver
import com.example.jobfinderapp.views.notifications.NotificationConstants
import javax.inject.Inject

class AlarmSchedulerImpl @Inject constructor (
    private val context: Context
) : AlarmScheduler {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun schedule(reminderEntity: ReminderEntity) {
        if (reminderEntity.triggerAtMillis > System.currentTimeMillis()) {

            // проверка exact alarm permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!alarmManager.canScheduleExactAlarms()) {
                    AppLogger.e("AlarmScheduler", "Exact alarm permission not granted")
                    return
                }
            }

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                reminderEntity.triggerAtMillis,
                createPendingIntent(reminderEntity))

        }
    }

    override fun cancel(reminderEntity: ReminderEntity) {
        alarmManager.cancel(createPendingIntent(reminderEntity))
    }

    override fun scheduleAgainAll(reminders: List<ReminderEntity>) {
        reminders.forEach {
            schedule(it)
        }
    }

    private fun createPendingIntent(reminderEntity: ReminderEntity): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(NotificationConstants.REMINDER_ID, reminderEntity.id)
            putExtra(NotificationConstants.REMINDER_JOB_ID, reminderEntity.jobID)
            putExtra(NotificationConstants.REMINDER_TITLE, reminderEntity.title)
            putExtra(NotificationConstants.REMINDER_MESSAGE, reminderEntity.message)
        }
        return PendingIntent.getBroadcast(
            context,
            reminderEntity.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

}