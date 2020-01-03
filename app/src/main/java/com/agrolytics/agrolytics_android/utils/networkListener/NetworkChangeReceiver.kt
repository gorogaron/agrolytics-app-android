package com.agrolytics.agrolytics_android.utils.networkListener

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager

class NetworkChangeReceiver : BroadcastReceiver() {

    private var eventBus = EventBus.instance

    private var firstConnect = true

    override fun onReceive(context: Context, intent: Intent) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val activeNetwork = cm.activeNetworkInfo
        val isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting
        if (isConnected) {
            if (firstConnect) {
                firstConnect = false
                eventBus.putNetworkStatus(NetworkStatus(NetworkStatus.State.ONLINE))
            }
        } else {
            firstConnect = true
            eventBus.putNetworkStatus(NetworkStatus(NetworkStatus.State.OFFLINE))
        }
    }
}