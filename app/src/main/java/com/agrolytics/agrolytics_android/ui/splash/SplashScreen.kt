package com.agrolytics.agrolytics_android.ui.splash

import com.agrolytics.agrolytics_android.base.BaseScreen

interface SplashScreen: BaseScreen {
    fun successfulAutoLogin()
    fun failedLogin()
}