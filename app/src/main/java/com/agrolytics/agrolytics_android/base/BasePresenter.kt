package com.agrolytics.agrolytics_android.base

import com.agrolytics.agrolytics_android.networking.AppServer
import com.agrolytics.agrolytics_android.utils.SessionManager
import io.reactivex.disposables.CompositeDisposable

abstract class BasePresenter<T> {

    var subscriptions: CompositeDisposable? = null
    var screen: T? = null
    var appServer: AppServer? = null
    var sessionManager: SessionManager? = null

    init { subscriptions = CompositeDisposable() }

    fun addInjections(arrayList: ArrayList<Any>) {
        for (item in arrayList) {
           when (item) {
               is AppServer -> appServer = item
               is SessionManager -> sessionManager = item
           }
        }
    }

    fun addView(view: T) {
        screen = view
    }

    fun removeView() {
        subscriptions?.clear()
        screen = null
    }
}