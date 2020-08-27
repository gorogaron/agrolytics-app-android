package com.agrolytics.agrolytics_android.ui.splash

import android.os.Bundle
import com.agrolytics.agrolytics_android.R
import com.agrolytics.agrolytics_android.base.BaseActivity
import com.agrolytics.agrolytics_android.ui.main.MainActivity

class SplashActivity: BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        startActivity(MainActivity::class.java, Bundle(), false)
        finish()

    }

}