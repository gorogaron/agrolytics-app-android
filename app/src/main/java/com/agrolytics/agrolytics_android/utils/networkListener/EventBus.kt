package com.agrolytics.agrolytics_android.utils.networkListener

import io.reactivex.Flowable
import io.reactivex.processors.PublishProcessor

class EventBus {

    private val networkStatusPublishProcessor = PublishProcessor.create<NetworkStatus>()

    val networkStatus: Flowable<NetworkStatus>
        get() = networkStatusPublishProcessor

    fun putNetworkStatus(networkStatus: NetworkStatus) {
        networkStatusPublishProcessor.onNext(networkStatus)
    }

    companion object {
        val instance = EventBus()
    }
}