package com.example.jobfinderapp.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit


private val dateFormatter =
    SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

fun Long.toDateString(): String {
    return dateFormatter.format(Date(this))
}

fun Long.toTimeAgo(): String {
    val now = System.currentTimeMillis()
    val diff = now - this

    val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
    val hours = TimeUnit.MILLISECONDS.toHours(diff)
    val days = TimeUnit.MILLISECONDS.toDays(diff)

    return when {
        minutes < 1 -> "just now"
        minutes < 60 -> "$minutes minutes ago"
        hours < 24 -> "$hours hours ago"
        days < 7 -> "$days days ago"
        else -> this.toDateString()
    }
}
