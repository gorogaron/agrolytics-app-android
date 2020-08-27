package com.agrolytics.agrolytics_android.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import com.agrolytics.agrolytics_android.R
import com.agrolytics.agrolytics_android.base.BaseActivity
import com.agrolytics.agrolytics_android.database.tables.RoomModule
import com.agrolytics.agrolytics_android.networking.AppServer
import com.agrolytics.agrolytics_android.networking.model.ImageUploadResponse
import com.agrolytics.agrolytics_android.ui.imageFinished.UploadFinishedActivity
import com.agrolytics.agrolytics_android.ui.images.ImagesActivity
import com.agrolytics.agrolytics_android.ui.info.InfoActivity
import com.agrolytics.agrolytics_android.ui.setLength.LengthActivity
import com.agrolytics.agrolytics_android.utils.ConfigInfo.CAMERA_CAPTURE
import com.agrolytics.agrolytics_android.utils.extensions.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.nav_bar.*
import org.koin.android.ext.android.inject
import android.location.LocationManager
import android.content.Context
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.location.Location
import android.net.ConnectivityManager
import android.util.Log
import android.widget.Toast
import com.agrolytics.agrolytics_android.ui.cropper.CropperActivity
import com.agrolytics.agrolytics_android.ui.guide.GuideActivity
import com.agrolytics.agrolytics_android.ui.login.LoginActivity
import com.agrolytics.agrolytics_android.utils.*
import com.agrolytics.agrolytics_android.ui.rodSelector.RodSelectorActivity
import com.agrolytics.agrolytics_android.utils.ConfigInfo.CROPPER
import com.agrolytics.agrolytics_android.utils.ConfigInfo.PICK_IMAGE
import com.agrolytics.agrolytics_android.utils.networkListener.EventBus
import com.agrolytics.agrolytics_android.utils.networkListener.NetworkChangeReceiver
import com.agrolytics.agrolytics_android.utils.networkListener.NetworkStatus
import com.google.firebase.auth.FirebaseAuth
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.startActivityForResult
import org.jetbrains.anko.uiThread
import java.io.InputStream
import java.lang.Exception
import java.lang.IllegalArgumentException


class MainActivity : BaseActivity(), View.OnClickListener, MainScreen, BaseActivity.OnDialogActions {

    private val TAG = "MainActivity"

    private val presenter: MainPresenter by inject()
    private val appServer: AppServer by inject()
    private val sessionManager: SessionManager by inject()
    private val roomModule: RoomModule by inject()

    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        presenter.addView(this)
        presenter.addInjections(arrayListOf(appServer, roomModule, sessionManager))
        presenter.setActivity(this)

        doAsync { Detector.init(assets) }

        btn_open_camera.setOnClickListener(this)
        btn_open_gallery.setOnClickListener(this)
        btn_guide.setOnClickListener(this)
        drawerHomeBtn.setOnClickListener(this)
        tv_show_old_length.setOnClickListener(this)
        container_images.setOnClickListener(this)
        container_info.setOnClickListener(this)
        container_main_menu.setOnClickListener(this)
        container_map.setOnClickListener(this)
        container_set_length.setOnClickListener(this)
        container_sign_out.setOnClickListener(this)

        container_main_menu.setBackgroundColor(ContextCompat.getColor(this, R.color.lightGreen))
        tv_email.text = sessionManager.userEmail

        checkPermissions(false, true)
    }

    override fun onResume() {
        super.onResume()
        val length = sessionManager.length
        if (length != 0f) {
            tv_show_old_length.text = """${getString(R.string.len)}$length""" + " m"
        } else {
            tv_show_old_length.text = getString(R.string.length) + " m"
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_open_camera -> openCamera()
            R.id.btn_open_gallery -> openGallery()
            R.id.drawerHomeBtn -> drawer_layout.openDrawer(GravityCompat.START)
            R.id.btn_guide -> openActivity(MenuItem.GUIDE)
            R.id.container_images -> openActivity(MenuItem.IMAGES)
            R.id.container_info -> openActivity(MenuItem.INFO)
            R.id.container_main_menu -> openActivity(MenuItem.MAIN)
            R.id.container_map -> openActivity(MenuItem.MAP)
            R.id.container_set_length -> openActivity(MenuItem.LENGTH)
            R.id.tv_show_old_length -> openActivity(MenuItem.LENGTH)
            R.id.container_sign_out -> signOut()
        }
    }

    private fun signOut() {
        FirebaseAuth.getInstance().signOut()
        sessionManager.clearSession()
        doAsync {
            roomModule.database?.clearAllTables()
            uiThread {
                startActivity(LoginActivity::class.java, Bundle(), false)
                //finish()
            }
        }
    }

    override fun negativeButtonClicked() {}

    override fun positiveButtonClicked() {
        presenter.saveLocalImageItem()
    }

    private fun openActivity(menuItem: MenuItem) {
        when (menuItem) {
            MenuItem.LENGTH -> {
                if (MenuItem.LENGTH.tag != TAG) {
                    startActivity(LengthActivity::class.java, Bundle(), true)
                }
            }
            MenuItem.MAIN -> {
                if (MenuItem.MAIN.tag != TAG) {
                    startActivity(MainActivity::class.java, Bundle(), true)
                }
            }
            MenuItem.IMAGES -> {
                if (MenuItem.IMAGES.tag != TAG) {
                    startActivity(ImagesActivity::class.java, Bundle(), true)
                }
            }
            MenuItem.MAP -> {
                if (MenuItem.MAP.tag != TAG) {
                    Toast.makeText(this,"Map deleted", Toast.LENGTH_SHORT).show()
                }
            }
            MenuItem.INFO -> {
                if (MenuItem.INFO.tag != TAG) {
                    startActivity(InfoActivity::class.java, Bundle(), true)
                }
            }
            MenuItem.GUIDE -> {
                if (MenuItem.GUIDE.tag != TAG) {
                    startActivity(GuideActivity::class.java, Bundle(), true)
                }
            }
        }
        drawer_layout.closeDrawers()
    }

    private fun openGallery() {
        if (cameraPermGiven() && storagePermGiven() && locationPermGiven()) {
            pickImage()
        } else {
            checkPermissions(false, false)
        }
    }

    private fun openCamera() {
        if (cameraPermGiven() && storagePermGiven()) {
            startCamera()
        } else {
            checkPermissions(true, false)
        }
    }

    private fun pickImage() {
      intent = Intent(Intent.ACTION_GET_CONTENT)
      intent.setType("image/*")
      startActivityForResult(intent, PICK_IMAGE)
    }

    private fun startCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera")
        imageUri = contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
        )
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        startActivityForResult(intent, CAMERA_CAPTURE)
    }

    override fun onBackPressed() {
        finish()
        System.exit(0)
    }

    private fun checkPermissions(isCamera: Boolean, isDefault: Boolean) {
        Dexter.withActivity(this)
            .withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.CAMERA
            ).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (!isDefault) {
                        if (isCamera) {
                            startCamera()
                        } else {
                            pickImage()
                        }
                    }
                    //updateLocation()
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest>,
                    token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }
            }).check()
    }

    fun startCropper(imgUri: Uri){
        val intent = Intent(this, CropperActivity::class.java)
        intent.putExtra("IMAGE", imgUri)
        startActivity(intent)
    }

    override fun onStop() {
        super.onStop()
        try {
            //unregisterReceiver(networkChangeReceiver)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            CAMERA_CAPTURE -> {
                try {
                    val thumbnail = MediaStore.Images.Media.getBitmap(
                        contentResolver, imageUri
                    )
                    val uri = presenter.getImageUri(thumbnail)
                    Log.d("HGXQR", "MAIN ACTIVITY UNCROPPED bitmap height" + thumbnail.height)
                    startCropper(uri)
                } catch (e: Exception) {
                    Log.d("Camera", "Failed to get camera image Uri: ${e}")
                    e.printStackTrace()
                }
            }
            PICK_IMAGE -> {
                try {
                    var imageUri = data!!.getData()
                    startCropper(imageUri)
                }
                catch (e: Exception){
                    //showToast("Hiba a kép megnyitása közben.")
                }
            }
        }
    }

}