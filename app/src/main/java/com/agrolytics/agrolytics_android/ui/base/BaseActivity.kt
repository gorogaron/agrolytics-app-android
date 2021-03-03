package com.agrolytics.agrolytics_android.ui.base

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.agrolytics.agrolytics_android.AgrolyticsApp
import com.agrolytics.agrolytics_android.R
import com.agrolytics.agrolytics_android.types.ConfigInfo
import com.agrolytics.agrolytics_android.ui.measurement.MeasurementManager
import com.agrolytics.agrolytics_android.ui.measurement.utils.ImageObtainer
import com.agrolytics.agrolytics_android.utils.fragments.CustomFragmentManager

abstract class BaseActivity : AppCompatActivity(), BaseScreen {

    private var dialog: AlertDialog? = null

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

    override fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun getContext(): Context {
        return this
    }

    override fun startActivity(activityToLoad: Class<out Activity>, bundle: Bundle, isFromMain: Boolean) {
        val intent = Intent(this, activityToLoad)
        intent.putExtras(bundle)
        startActivity(intent)
        if (!isFromMain) {
            finish()
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        //Can only return from camera or browser activity
        if (resultCode == Activity.RESULT_OK ){
            when (requestCode) {
                ConfigInfo.IMAGE_CAPTURE -> {
                    MeasurementManager.startCropperActivity(this, ImageObtainer.cameraImageUri)
                }
                ConfigInfo.IMAGE_BROWSE -> {
                    if (intent?.data != null){
                        MeasurementManager.startCropperActivity(this, intent.data!!)
                    }
                }
            }
        }
    }
}