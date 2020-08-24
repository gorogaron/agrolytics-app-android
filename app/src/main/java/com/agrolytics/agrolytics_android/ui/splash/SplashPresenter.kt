package com.agrolytics.agrolytics_android.ui.splash

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.agrolytics.agrolytics_android.base.BasePresenter
import com.agrolytics.agrolytics_android.networking.AppServer
import com.agrolytics.agrolytics_android.ui.login.LoginActivity
import com.agrolytics.agrolytics_android.utils.Util
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.joda.time.DateTime
import java.util.*
import java.util.concurrent.TimeUnit

class SplashPresenter(val context: Context): BasePresenter<SplashScreen>() {

    private val TAG = "SplashPresenter"

    fun checkExpire(currentUser: FirebaseUser?) {
        currentUser?.let {
            if (Util.isNetworkAvailable(context)) {
                currentUser.getIdToken(false).addOnSuccessListener { userToken ->
                    appServer?.updateApiService(userToken.token)
                    getUser(currentUser)
                }.addOnFailureListener { e ->
                    screen?.showToast("Something went wrong.")
                }
            } else {
                val timer = Observable.timer(1000, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        val firstLogin = sessionManager?.firstLogin
                        if (firstLogin != null && firstLogin.isNotEmpty()) {
                            val firstLoginDate = DateTime.parse(firstLogin)
                            val firstLoginDateThirtyAdded = firstLoginDate.plusDays(30)
                            val currentDate = DateTime.now()

                            val msDiff = firstLoginDateThirtyAdded.millis - currentDate.millis
                            val daysDiff = TimeUnit.MILLISECONDS.toDays(msDiff)

                            if (daysDiff > 0) {
                                screen?.successfulAutoLogin()
                            } else {
                                logout()
                            }
                        } else {
                            logout()
                        }
                    }
                subscriptions?.add(timer)
            }
        } ?: run {
            screen?.failedLogin()
        }
    }

    private fun logout() {
        doAsync {
            roomModule?.database?.clearAllTables()
            uiThread {
                screen?.failedLogin()
                FirebaseAuth.getInstance().signOut()
                sessionManager?.clearSession()
            }
        }
    }

    private fun getUser(user: FirebaseUser?) {
        Log.d("Firebase", "Getting user from firebase...")
        user?.let { fbUser ->
            val userId = fbUser.uid
            val userColRef = fireStoreDB?.db?.collection("user")?.document(userId)
            userColRef?.get()
                ?.addOnSuccessListener { document ->
                    if (document != null) {
                        Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                        val firstLogin = document["first_login"] as String
                        val firstLoginDate = DateTime.parse(firstLogin)
                        val firstLoginDateThirtyAdded = firstLoginDate.plusDays(30)
                        val currentDate = DateTime.now()

                        val msDiff = firstLoginDateThirtyAdded.millis - currentDate.millis
                        val daysDiff = TimeUnit.MILLISECONDS.toDays(msDiff)

                        if (daysDiff > 0) {
                            screen?.successfulAutoLogin()
                        } else {
                            logout()
                        }
                    } else {
                        screen?.showToast("Something went wrong.")
                        Log.d("Firebase", "Could not get user from firebase: document is null")
                    }
                }
                ?.addOnFailureListener { exception ->
                    screen?.showToast("Something went wrong.")
                    Log.d("Firebase", "Could not get user from firebase: ${exception}")
                }
        }
    }
}