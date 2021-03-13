package com.agrolytics.agrolytics_android.ui.base

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import com.agrolytics.agrolytics_android.R
import com.agrolytics.agrolytics_android.types.ConfigInfo
import com.agrolytics.agrolytics_android.ui.measurement.MeasurementManager
import com.agrolytics.agrolytics_android.ui.measurement.utils.ImageObtainer
import com.agrolytics.agrolytics_android.utils.connection.ConnectionLiveData
import org.jetbrains.anko.contentView

abstract class BaseActivity : AppCompatActivity(), BaseScreen {

    private var dialog: AlertDialog? = null

   //TODO: Jobb lenne singleton livadata-t létrehozni
    protected lateinit var connectionLiveData : ConnectionLiveData
    var isInternetAvailable = false
    lateinit var mContentView : ViewGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        connectionLiveData =
            ConnectionLiveData(this)
        connectionLiveData.observe(this, Observer {value ->
            isInternetAvailable = value
        })
    }

    override fun setContentView(layoutResID: Int) {
        mContentView = layoutInflater.inflate(layoutResID, null) as ViewGroup
        setContentView(mContentView)
    }

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

    fun show2OptionDialog(title : String,
                          button1Text : String,
                          button2Text : String,
                          listener1: (() -> Unit),
                          listener2: (() -> Unit),
                          cancelable : Boolean = true)
    {
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(cancelable)
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_2_options, null, false)
        builder.setView(view)
        val dialog = builder.create()

        view.findViewById<TextView>(R.id.title).text = title

        view.findViewById<ConstraintLayout>(R.id.button_1).apply {
            findViewById<TextView>(R.id.buttonText).text = button1Text
            setOnClickListener{
                listener1()
                dialog.dismiss()
            }
        }
        view.findViewById<ConstraintLayout>(R.id.button_2).apply {
            findViewById<TextView>(R.id.buttonText).text = button2Text
            setOnClickListener{
                listener2()
                dialog.dismiss()
            }
        }

        dialog.window!!.setBackgroundDrawableResource(R.drawable.bg_white_round)
        dialog.show()
    }
}