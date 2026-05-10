package com.example.jobfinderapp.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.jobfinderapp.App
import com.example.jobfinderapp.data.AppRepository
import com.example.jobfinderapp.utils.AlarmScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var repo: AppRepository

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

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