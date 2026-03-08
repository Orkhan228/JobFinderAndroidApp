package com.example.jobfinderapp.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject

class NetworkMonitorImpl @Inject constructor(private val context: Context) : NetworkMonitor{

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _isConnected = MutableLiveData<Boolean>()
    override val isConnected: LiveData<Boolean> = _isConnected

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {

        override fun onAvailable(network: Network) {
            _isConnected.postValue(true)
        }

        override fun onLost(network: Network) {
            _isConnected.postValue(false)
        }

    }

    init {
        //Потому что NetworkCallback не вызовется сразу. Он срабатывает только когда: сеть появилась сеть пропала
        //Но если приложение уже запустилось без интернета — callback ничего не вызовет.
        //Поэтому нам нужно самим проверить текущее состояние.
        val currentState = connectivityManager.activeNetwork
        _isConnected.value = currentState != null

        connectivityManager.registerDefaultNetworkCallback(networkCallback)
    }

}