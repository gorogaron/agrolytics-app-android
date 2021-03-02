package com.agrolytics.agrolytics_android.ui.base

import com.agrolytics.agrolytics_android.data.DataClient
import com.agrolytics.agrolytics_android.network.AppServer
import com.agrolytics.agrolytics_android.utils.SessionManager
import com.google.firebase.auth.FirebaseAuth

abstract class BasePresenter<T> {

    var screen: T? = null
    var appServer: AppServer? = null
    var sessionManager: SessionManager? = null
    var auth: FirebaseAuth? = null
    var dataClient: DataClient? = null

    fun addInjections(arrayList: ArrayList<Any>) {
        for (item in arrayList) {
           when (item) {
               is AppServer -> appServer = item
               is SessionManager -> sessionManager = item
               is FirebaseAuth -> auth = item
               is DataClient -> dataClient = item
           }
        }
    }

    fun addView(view: T) {
        screen = view
    }

    fun removeView() {
        screen = null
    }
}