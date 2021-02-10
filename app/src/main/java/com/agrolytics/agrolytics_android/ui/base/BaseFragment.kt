package com.agrolytics.agrolytics_android.ui.base

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

abstract class BaseFragment: Fragment(), BaseScreen {

    protected abstract fun getLayoutId(): Int

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(getLayoutId(), container, false)
    }


    override fun hideLoading() {
        if (activity is BaseActivity) {
            (activity as BaseActivity).hideLoading()
        }
    }

    override fun showLoading() {
        if (activity is BaseActivity) {
            (activity as BaseActivity).showLoading()
        }
    }

    override fun showToast(message: String) {
        if (activity is BaseActivity) {
            (activity as BaseActivity).showToast(message)
        }
    }

    override fun setToolbarTitle(title: String) {
        if (activity is BaseActivity) {
            (activity as BaseActivity).setToolbarTitle(title)
        }
    }

    override fun showFragment(fragment: BaseFragment, tag: String) {
        if (activity is BaseActivity) {
            (activity as BaseActivity).showFragment(fragment,tag)
        }
    }

    override fun showFragmentBack(fragment: BaseFragment, tag: String) {
        if (activity is BaseActivity) {
            (activity as BaseActivity).showFragmentBack(fragment,tag)
        }
    }

    override fun showFragmentForward(fragment: BaseFragment, tag: String) {
        if (activity is BaseActivity) {
            (activity as BaseActivity).showFragmentForward(fragment,tag)
        }
    }

    override fun startActivityBack(activityToLoad: Class<out Activity>, bundle: Bundle) {
        if (activity is BaseActivity) {
            (activity as BaseActivity).startActivityBack(activityToLoad, bundle)
        }
    }

    override fun startActivityForward(activityToLoad: Class<out Activity>, bundle: Bundle) {
        if (activity is BaseActivity) {
            (activity as BaseActivity).startActivityForward(activityToLoad, bundle)
        }
    }

    override fun startActivity(activityToLoad: Class<out Activity>, bundle: Bundle, isFromMain: Boolean) {
        if (activity is BaseActivity) {
            (activity as BaseActivity).startActivity(activityToLoad, bundle, isFromMain)
        }
    }

    override fun startCustomActivityForResult(activityToLoad: Class<out Activity>, bundle: Bundle, requestCode: Int) {
        if (activity is BaseActivity) {
            (activity as BaseActivity).startCustomActivityForResult(activityToLoad, bundle, requestCode)
        }
    }

    override fun showToolbar(show: Boolean) {
        if (activity is BaseActivity) {
            (activity as BaseActivity).showToolbar(show)
        }
    }

    override fun exitApp() {
        if (activity is BaseActivity) {
            (activity as BaseActivity).exitApp()
        }
    }

    override fun clearFragments() {
        if (activity is BaseActivity) {
            (activity as BaseActivity).clearFragments()
        }
    }

    override fun showAlertDialog(title: String, message: String, listener: BaseActivity.OnDialogActions, cancelable: Boolean, positiveButtonText: String) {
        if (activity is BaseActivity) {
            (activity as BaseActivity).showAlertDialog(title,message,listener, cancelable, positiveButtonText)
        }
    }

    override fun showAlertDialog(title: String?, message: String?) {
        if (activity is BaseActivity) {
            (activity as BaseActivity).showAlertDialog(title,message)
        }
    }

    override fun getContext(): Context {
        return activity!!
    }
}