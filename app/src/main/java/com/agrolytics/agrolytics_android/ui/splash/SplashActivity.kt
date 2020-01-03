package com.agrolytics.agrolytics_android.ui.splash

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityOptionsCompat
import com.agrolytics.agrolytics_android.R
import com.agrolytics.agrolytics_android.base.BaseActivity
import com.agrolytics.agrolytics_android.database.firebase.FireStoreDB
import com.agrolytics.agrolytics_android.database.tables.RoomModule
import com.agrolytics.agrolytics_android.ui.login.LoginActivity
import com.agrolytics.agrolytics_android.ui.main.MainActivity
import com.agrolytics.agrolytics_android.utils.SessionManager
import com.google.firebase.auth.FirebaseAuth
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_login.*
import org.koin.android.ext.android.inject
import java.util.concurrent.TimeUnit

class SplashActivity: BaseActivity(), SplashScreen {

    private val presenter: SplashPresenter by inject()
    private val sessionManager: SessionManager by inject()
    private val fireStoreDB: FireStoreDB by inject()
    private val roomModule: RoomModule by inject()

    private var auth: FirebaseAuth? = null
    private var subscription: CompositeDisposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        auth = FirebaseAuth.getInstance()

        val currentUser = auth?.currentUser

        presenter.addView(this)
        presenter.addInjections(arrayListOf(sessionManager, fireStoreDB, roomModule))

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
        //val activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(this, iv_logo, "logoImage")
        val intent = Intent(this, MainActivity::class.java)
//        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
//        startActivity(intent, activityOptionsCompat.toBundle())
        startActivity(MainActivity::class.java, Bundle(), false)
        finish()
    }

    override fun failedLogin() {
        //val activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(this, iv_logo, "logoImage")
        val intent = Intent(this, LoginActivity::class.java)
//        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
//        startActivity(intent, activityOptionsCompat.toBundle())
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