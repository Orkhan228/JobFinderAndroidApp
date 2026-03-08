package com.example.jobfinderapp.utils

import androidx.lifecycle.LiveData

interface NetworkMonitor {
    val isConnected: LiveData<Boolean>
}