package com.example.jobfinderapp.utils

import android.util.Log
import com.example.jobfinderapp.BuildConfig

object AppLogger {

    fun d(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, message)
        }
    }

    fun e(tag: String, message: String, tr: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, message, tr)
        }
    }

}