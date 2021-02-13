package com.agrolytics.agrolytics_android.ui.base

import com.agrolytics.agrolytics_android.database.DataClient
import com.agrolytics.agrolytics_android.database.firestore.FireStoreDB
import com.agrolytics.agrolytics_android.database.local.RoomModule
import com.agrolytics.agrolytics_android.networking.AppServer
import com.agrolytics.agrolytics_android.utils.SessionManager
import com.google.firebase.auth.FirebaseAuth
import io.reactivex.disposables.CompositeDisposable

abstract class BasePresenter<T> {

    // TODO: clear subscriptions when needed
    var subscriptions: CompositeDisposable? = null
    var screen: T? = null
    var appServer: AppServer? = null
    var sessionManager: SessionManager? = null
    var roomModule: RoomModule? = null
    var fireStoreDB: FireStoreDB? = null
    var auth: FirebaseAuth? = null
    var dataClient: DataClient? = null

    init { subscriptions = CompositeDisposable() }

    fun addInjections(arrayList: ArrayList<Any>) {
        for (item in arrayList) {
           when (item) {
               is AppServer -> appServer = item
               is SessionManager -> sessionManager = item
               is RoomModule -> roomModule = item
               is FireStoreDB -> fireStoreDB = item
               is FirebaseAuth -> auth = item
               is DataClient -> dataClient = item
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