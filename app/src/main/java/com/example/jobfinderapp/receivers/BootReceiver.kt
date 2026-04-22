package com.example.jobfinderapp.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.jobfinderapp.App
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val appComponent = (context.applicationContext as App).appComponent
        val repo = appComponent.getAppRepository()
        val alarmScheduler = appComponent.getAlarmScheduler()

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val reminders = repo.getRemindersListByOnce()
                alarmScheduler.scheduleAgainAll(reminders)
            } finally {
                pendingResult.finish()
            }
        }
    }
}