package com.example.jobfinderapp.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class NetworkMonitorImpl @Inject constructor(context: Context) : NetworkMonitor {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _isConnected = MutableStateFlow<Boolean>(false)
    override val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {

        override fun onAvailable(network: Network) {
            _isConnected.value = true
        }

        override fun onLost(network: Network) {
            _isConnected.value = false
        }

    }

    init {
        //Потому что NetworkCallback не вызовется сразу. Он срабатывает только когда: сеть появилась сеть пропала
        //Но если приложение уже запустилось без интернета — callback ничего не вызовет.
        //Поэтому нам нужно самим проверить текущее состояние.
        _isConnected.value = isCurrentlyConnected()
        connectivityManager.registerDefaultNetworkCallback(networkCallback)
    }


    private fun isCurrentlyConnected(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false

        val capabilities =
            connectivityManager.getNetworkCapabilities(network)
                ?: return false

        return capabilities.hasCapability(
            NetworkCapabilities.NET_CAPABILITY_VALIDATED
        )
    }

}