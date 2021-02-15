package com.agrolytics.agrolytics_android.ui.base

import android.app.Activity
import android.content.Context
import android.os.Bundle

interface BaseScreen {
    fun showLoading()
    fun hideLoading()
    fun exitApp()
    fun showToolbar(show: Boolean)
    fun startActivityForward(activityToLoad: Class<out Activity>, bundle: Bundle)
    fun startActivity(activityToLoad: Class<out Activity>, bundle: Bundle, isFromMain: Boolean)
    fun startCustomActivityForResult(activityToLoad: Class<out Activity>, bundle: Bundle, requestCode: Int)
    fun startActivityBack(activityToLoad: Class<out Activity>, bundle: Bundle)
    fun setToolbarTitle(title: String)
    fun showFragmentBack(fragment: BaseFragment, tag: String)
    fun showFragmentForward(fragment: BaseFragment, tag: String)
    fun showFragment(fragment: BaseFragment, tag: String)
    fun clearFragments()
    fun showToast(message: String)
    fun showAlertDialog(title: String, message: String,
                        listener: BaseActivity.OnDialogActions, cancelable: Boolean, positiveButtonText: String)
    fun showAlertDialog(title: String?, message: String?)
    fun getContext(): Context
}