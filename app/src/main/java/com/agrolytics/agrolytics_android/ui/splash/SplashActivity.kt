package com.agrolytics.agrolytics_android.ui.splash

import android.os.Bundle
import android.util.Log
import com.agrolytics.agrolytics_android.R
import com.agrolytics.agrolytics_android.base.BaseActivity
import com.agrolytics.agrolytics_android.database.firebase.FireStoreDB
import com.agrolytics.agrolytics_android.database.tables.RoomModule
import com.agrolytics.agrolytics_android.networking.AppServer
import com.agrolytics.agrolytics_android.ui.login.LoginActivity
import com.agrolytics.agrolytics_android.ui.main.MainActivity
import com.agrolytics.agrolytics_android.utils.SessionManager
import com.google.firebase.auth.FirebaseAuth
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.koin.android.ext.android.inject
import java.util.concurrent.TimeUnit

class SplashActivity: BaseActivity(), SplashScreen {

    private val presenter: SplashPresenter by inject()
    private val sessionManager: SessionManager by inject()
    private val fireStoreDB: FireStoreDB by inject()
    private val roomModule: RoomModule by inject()
    private val appServer: AppServer by inject()
    private var auth: FirebaseAuth? = null
    private var subscription: CompositeDisposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        auth = FirebaseAuth.getInstance()

        val currentUser = auth?.currentUser

        presenter.addView(this)
        presenter.addInjections(arrayListOf(sessionManager, fireStoreDB, roomModule, appServer))

        if (currentUser != null) {
            presenter.checkExpire(currentUser)
        } else {
            val timer = Observable.timer(1000, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                        failedLogin()
                }
            subscription?.add(timer)
        }
    }

    override fun successfulAutoLogin() {
        startActivity(MainActivity::class.java, Bundle(), false)
        finish()
    }

    override fun failedLogin() {
        startActivity(LoginActivity::class.java, Bundle(), false)
        finish()
    }

    override fun onStop() {
        super.onStop()
        subscription?.clear()
        finish()
        Log.d("Splash","onStop")
    }

    override fun onPause() {
        Log.d("Splash","onPause")
        super.onPause()
    }

    override fun onDestroy() {
        Log.d("Splash","onDestroy")
        super.onDestroy()
    }
}