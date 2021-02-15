package com.agrolytics.agrolytics_android.ui.measurement.utils

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import com.agrolytics.agrolytics_android.types.ConfigInfo
import com.agrolytics.agrolytics_android.utils.permissions.*


/**This class is responsible for opening the camera or image browser application. The result can be
 * obtained in the calling Activity.*/

object ImageObtainer {

    lateinit var callingActivity : Activity
    lateinit var cameraImageUri : Uri

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
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "CameraImage")
        cameraImageUri = callingActivity.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)!!
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri)
        callingActivity.startActivityForResult(intent, ConfigInfo.IMAGE_CAPTURE)
    }
}