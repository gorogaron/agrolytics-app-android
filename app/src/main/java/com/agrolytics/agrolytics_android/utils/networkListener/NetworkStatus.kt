package com.agrolytics.agrolytics_android.utils.networkListener

class NetworkStatus(var state: State?) {
    enum class State {
        ONLINE,
        OFFLINE
    }
}