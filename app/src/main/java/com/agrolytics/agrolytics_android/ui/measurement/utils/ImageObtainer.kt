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

    lateinit var cameraImageUri : Uri

    //TODO: Add location permission check before starting measurement
    fun openGallery(callingActivity : Activity) {
        if (callingActivity.storagePermGiven()) {
            startImageBrowserActivity(callingActivity)
        } else {
            val permissionCheckedListener = createPermissionCheckListener(::startImageBrowserActivity, callingActivity)
            callingActivity.requestForAllPermissions(callingActivity, permissionCheckedListener)
        }
    }

    fun openCamera(callingActivity : Activity) {
        if (callingActivity.cameraPermGiven() && callingActivity.storagePermGiven()) {
            startCameraActivity(callingActivity)
        } else {
            val permissionCheckedListener = createPermissionCheckListener(::startCameraActivity, callingActivity)
            callingActivity.requestForAllPermissions(callingActivity, permissionCheckedListener)
        }
    }

    private fun startImageBrowserActivity(callingActivity : Activity) {
        val intent = Intent(Intent.ACTION_GET_CONTENT).setType("image/*")
        callingActivity.startActivityForResult(intent, ConfigInfo.IMAGE_BROWSE)
    }

    private fun startCameraActivity(callingActivity : Activity) {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "CameraImage")
        cameraImageUri = callingActivity.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)!!
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri)
        callingActivity.startActivityForResult(intent, ConfigInfo.IMAGE_CAPTURE)
    }
}