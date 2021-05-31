package com.agrolytics.agrolytics_android.ui.base

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle

interface BaseScreen {
    fun showLoading(cancelable : Boolean = false, cancelListener : DialogInterface.OnCancelListener? = null)
    fun hideLoading()
    fun exitApp()
    fun showToast(message: String)
    fun getContext(): Context
    fun startActivity(activityToLoad: Class<out Activity>, bundle: Bundle, isFromMain: Boolean)
}