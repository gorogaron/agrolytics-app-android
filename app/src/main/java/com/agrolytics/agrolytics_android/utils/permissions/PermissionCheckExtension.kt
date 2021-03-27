package com.agrolytics.agrolytics_android.utils.permissions

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.agrolytics.agrolytics_android.ui.measurement.utils.ImageObtainer
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener

/** This class extends the Activity objects with permission checking function*/

fun Activity.locationPermGiven(): Boolean {
    return ActivityCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}

fun Activity.storagePermGiven(): Boolean {
    return ActivityCompat.checkSelfPermission(
        this,
        android.Manifest.permission.READ_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
        this,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED
}

fun Activity.cameraPermGiven(): Boolean {
    return ActivityCompat.checkSelfPermission(
        this,
        android.Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED
}

fun Activity.requestForAllPermissions(activity: Activity, listener : MultiplePermissionsListener? = null) {
    Dexter.withActivity(activity)
        .withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CAMERA
        ).withListener(listener).check()
}

//TODO: Lehet, hogy ez nem is kell, mert a main activity-t bezárjuk, ha nem kapunk meg valameilyen engedélyt
fun createPermissionCheckListener(onPermissionCheckedAction : ((Activity) -> Unit)? = null, callingActivity: Activity) : MultiplePermissionsListener{
    return object : MultiplePermissionsListener {
        override fun onPermissionsChecked(report: MultiplePermissionsReport) {
            if (onPermissionCheckedAction != null){
                onPermissionCheckedAction(callingActivity)
            }
        }

        override fun onPermissionRationaleShouldBeShown(permissions: List<PermissionRequest>, token: PermissionToken) {
            token.continuePermissionRequest()
        }
    }
}