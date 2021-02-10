package com.agrolytics.agrolytics_android.ui.measurement.utils

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.provider.MediaStore
import androidx.core.app.ActivityCompat.startActivityForResult
import com.agrolytics.agrolytics_android.utils.ConfigInfo
import com.agrolytics.agrolytics_android.utils.permissions.cameraPermGiven
import com.agrolytics.agrolytics_android.utils.permissions.defaultPermissionCheckListener
import com.agrolytics.agrolytics_android.utils.permissions.locationPermGiven
import com.agrolytics.agrolytics_android.utils.permissions.storagePermGiven
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener

/**This class is responsible for opening the camera or image browser application. The result can be
 * obtained in the calling Activity.*/

object ImageHooker {

    lateinit var callingActivity : Activity
    val permissionCheckListener_startCamera = object : MultiplePermissionsListener {
        override fun onPermissionsChecked(report: MultiplePermissionsReport) {
            browseImageActivity()
        }

        override fun onPermissionRationaleShouldBeShown(permissions: List<PermissionRequest>, token: PermissionToken) {
            token.continuePermissionRequest()
        }
    }

    val permissionCheckListener_startImageBrowser = object : MultiplePermissionsListener {
        override fun onPermissionsChecked(report: MultiplePermissionsReport) {
            //DO NOTHING
            defaultPermissionCheckListener
        }

        override fun onPermissionRationaleShouldBeShown(permissions: List<PermissionRequest>, token: PermissionToken) {
            token.continuePermissionRequest()
        }
    }

    fun setActivity(activity: Activity) {
        callingActivity = activity
    }

    fun openGallery() {
        if (callingActivity.cameraPermGiven() && callingActivity.storagePermGiven() && callingActivity.locationPermGiven()) {
            browseImageActivity()
        } else {
            checkPermissions(isCamera = false, isDefault = false)
        }
    }

    fun openCamera(context: Context) {
        if (callingActivity.cameraPermGiven() && callingActivity.storagePermGiven()) {
            startCameraActivity()
        } else {



            requestForAllPermissions(a)
        }
    }



    private fun startImageBrowserActivity() {
        var intent = Intent(Intent.ACTION_GET_CONTENT).setType("image/*")
        startActivityForResult(intent, ConfigInfo.IMAGE_BROWSE)
    }

    private fun startCameraActivity() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera")
        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)!!
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        startActivityForResult(intent, ConfigInfo.IMAGE_CAPTURE)
    }
}