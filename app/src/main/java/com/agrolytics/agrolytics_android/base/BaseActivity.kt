package com.agrolytics.agrolytics_android.base

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.agrolytics.agrolytics_android.R
import com.agrolytics.agrolytics_android.utils.fragments.CustomFragmentManager

abstract class BaseActivity : AppCompatActivity(), BaseScreen {

    var toolbarText: TextView? = null
    private var dialog: AlertDialog? = null
    protected val fragmentManager =
        CustomFragmentManager(supportFragmentManager)

    override fun showLoading() {
        dialog = AlertDialog.Builder(this).create()
        val factory = LayoutInflater.from(this)
        val customView = factory.inflate(R.layout.custom_progress_bar, null)
        dialog?.setCancelable(false)
        dialog?.setView(customView)
        dialog?.show()
    }

    override fun hideLoading() {
        dialog?.dismiss()
        dialog = null
    }

    override fun exitApp() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun showToolbar(show: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun startActivityForward(activityToLoad: Class<out Activity>, bundle: Bundle) {
        val intent = Intent(this, activityToLoad)
        if (!bundle.isEmpty) {
            intent.putExtra("extra", bundle)
        }
        startActivity(intent)
        this.finish()
        this.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    override fun startActivityBack(activityToLoad: Class<out Activity>, bundle: Bundle) {
        val intent = Intent(this, activityToLoad)
        if (!bundle.isEmpty) {
            intent.putExtra("extra", bundle)
        }
        startActivity(intent)
        this.finish()
        this.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    override fun startActivity(activityToLoad: Class<out Activity>, bundle: Bundle, isFromMain: Boolean) {
        val intent = Intent(this, activityToLoad)
        intent.putExtras(bundle)
        startActivity(intent)
        if (!isFromMain) {
            finish()
        }
    }

    override fun startCustomActivityForResult(activityToLoad: Class<out Activity>, bundle: Bundle, requestCode: Int) {
        val intent = Intent(this, activityToLoad)
        intent.putExtras(bundle)
        startActivityForResult(intent, requestCode)
    }

    override fun setToolbarTitle(title: String) {
        toolbarText?.let { it.text = title }
    }

    override fun showFragmentBack(fragment: BaseFragment, tag: String) {
        fragmentManager.showFragmentBack(fragment, tag)
    }

    override fun showFragmentForward(fragment: BaseFragment, tag: String) {
        fragmentManager.showFragmentForward(fragment, tag)
    }

    override fun showFragment(fragment: BaseFragment, tag: String) {
        fragmentManager.showFragment(fragment, tag)
    }

    override fun clearFragments() {
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    override fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun showAlertDialog(title: String?, message: String?) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok) { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }

    override fun showAlertDialog(title: String, message: String, listener: OnDialogActions, cancelable: Boolean, positiveButtonText: String) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setCancelable(cancelable)
            .setPositiveButton(positiveButtonText) { dialog, which ->
                listener.positiveButtonClicked()
            }
            .setNegativeButton("Mégse") { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }

    override fun getContext(): Context {
        return this
    }

    interface OnDialogActions {
        fun positiveButtonClicked()
        fun negativeButtonClicked()
    }
}