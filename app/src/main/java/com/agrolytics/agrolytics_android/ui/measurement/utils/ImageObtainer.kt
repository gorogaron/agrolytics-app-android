package com.agrolytics.agrolytics_android.ui.measurement.utils

import android.app.Activity
import android.content.Intent
import android.provider.MediaStore
import com.agrolytics.agrolytics_android.utils.ConfigInfo
import com.agrolytics.agrolytics_android.utils.permissions.*


/**This class is responsible for opening the camera or image browser application. The result can be
 * obtained in the calling Activity.*/

object ImageObtainer {

    lateinit var callingActivity : Activity

    fun setActivity(activity: Activity) {
        callingActivity = activity
    }

    //TODO: Add location permission check before starting measurement
    fun openGallery() {
        if (callingActivity.storagePermGiven()) {
            startImageBrowserActivity()
        } else {
            val permissionCheckedListener = createPermissionCheckListener(::startImageBrowserActivity)
            callingActivity.requestForAllPermissions(callingActivity, permissionCheckedListener)
        }
    }

    fun openCamera() {
        if (callingActivity.cameraPermGiven() && callingActivity.storagePermGiven()) {
            startCameraActivity()
        } else {
            val permissionCheckedListener = createPermissionCheckListener(::startCameraActivity)
            callingActivity.requestForAllPermissions(callingActivity, permissionCheckedListener)
        }
    }

    private fun startImageBrowserActivity() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).setType("image/*")
        callingActivity.startActivityForResult(intent, ConfigInfo.IMAGE_BROWSE)
    }

    private fun startCameraActivity() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        callingActivity.startActivityForResult(intent, ConfigInfo.IMAGE_CAPTURE)
    }
}