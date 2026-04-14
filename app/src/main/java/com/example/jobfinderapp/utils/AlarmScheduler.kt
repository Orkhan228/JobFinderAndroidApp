package com.example.jobfinderapp.utils

import com.example.jobfinderapp.data.entity.ReminderEntity

interface AlarmScheduler {
    fun schedule(reminderEntity: ReminderEntity)
    fun cancel(reminderEntity: ReminderEntity)
    fun scheduleAgainAll(reminders: List<ReminderEntity>)
}