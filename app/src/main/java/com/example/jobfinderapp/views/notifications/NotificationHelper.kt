package com.example.jobfinderapp.views.notifications

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.navigation.NavDeepLinkBuilder
import com.example.jobfinderapp.R
import com.example.jobfinderapp.utils.AppLogger

object NotificationHelper {

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NotificationConstants.CHANNEL_ID,
                NotificationConstants.CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = NotificationConstants.CHANNEL_DESCRIPTION
            }

            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun createReminderNotification(
        context: Context,
        jobID: String,
        title: String,
        message: String?,
        notificationId: Int
    ) {
        val args = bundleOf(NotificationConstants.REMINDER_JOB_ID to jobID)

        val pendingIntentToDetailsFragment = NavDeepLinkBuilder(context)
            .setGraph(R.navigation.fragments_navigation)
            .setDestination(R.id.detailsFragment)
            .setArguments(args)
            .createPendingIntent()

        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(context, NotificationConstants.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle(title)
                .setContentText(message ?: "You have a reminder")
                .setContentIntent(pendingIntentToDetailsFragment)
                .setAutoCancel(true)
                .setShowWhen(true)
        } else {
            Notification.Builder(context)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle(title)
                .setContentText(message ?: "You have a reminder")
                .setContentIntent(pendingIntentToDetailsFragment)
                .setAutoCancel(true)
                .setShowWhen(true)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED


            if (!granted) {
                AppLogger.e("NotificationHelper", "Permission POST_NOTIFICATIONS not granted")
                return
            }
        }

        try {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            AppLogger.e("NotificationHelper", e.message.toString(), e)
        }

    }

}