package com.agrolytics.agrolytics_android.ui.splash

import android.os.Bundle
import com.agrolytics.agrolytics_android.R
import com.agrolytics.agrolytics_android.ui.base.BaseActivity
import com.agrolytics.agrolytics_android.ui.login.LoginActivity
import com.agrolytics.agrolytics_android.utils.SessionManager
import com.agrolytics.agrolytics_android.utils.Util
import org.koin.android.ext.android.inject

class SplashActivity: BaseActivity() {

    val sessionManager: SessionManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        //Nyelv beállítása, ha alapértelmezettről el lett állítva
        if (sessionManager.language != "") {
            Util.setLocale(this, sessionManager.language)
        }

        startActivity(LoginActivity::class.java, Bundle(), false)
    }
}