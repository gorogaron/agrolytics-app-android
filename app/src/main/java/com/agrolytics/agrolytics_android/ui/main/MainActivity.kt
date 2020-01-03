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
import com.agrolytics.agrolytics_android.networking.model.ResponseImageUpload
import com.agrolytics.agrolytics_android.ui.imageFinished.UploadFinishedActivity
import com.agrolytics.agrolytics_android.ui.images.ImagesActivity
import com.agrolytics.agrolytics_android.ui.info.InfoActivity
import com.agrolytics.agrolytics_android.ui.map.MapActivity
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
import com.agrolytics.agrolytics_android.ui.login.LoginActivity
import com.agrolytics.agrolytics_android.utils.*
import com.agrolytics.agrolytics_android.ui.rodSelector.RodSelectorActivity
import com.agrolytics.agrolytics_android.utils.networkListener.EventBus
import com.agrolytics.agrolytics_android.utils.networkListener.NetworkChangeReceiver
import com.agrolytics.agrolytics_android.utils.networkListener.NetworkStatus
import com.google.firebase.auth.FirebaseAuth
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.lang.Exception
import java.lang.IllegalArgumentException


class MainActivity : BaseActivity(), View.OnClickListener, MainScreen, BaseActivity.OnDialogActions {

    private val TAG = "MainActivity"

    private val presenter: MainPresenter by inject()
    private val appServer: AppServer by inject()
    private val sessionManager: SessionManager by inject()
    private val roomModule: RoomModule by inject()

    private var locationManager: LocationManager? = null
    private var locationListener: AgroLocationListener? = null
    private var eventBusDisposable: Disposable? = null
    private val networkChangeReceiver: NetworkChangeReceiver = NetworkChangeReceiver()

    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        presenter.addView(this)
        presenter.addInjections(arrayListOf(appServer, roomModule, sessionManager))
        presenter.setActivity(this)

        btn_open_camera.setOnClickListener(this)
        btn_open_gallery.setOnClickListener(this)
        btn_info.setOnClickListener(this)
        drawerHomeBtn.setOnClickListener(this)
        tv_show_old_length.setOnClickListener(this)
        container_images.setOnClickListener(this)
        container_info.setOnClickListener(this)
        container_main_menu.setOnClickListener(this)
        container_map.setOnClickListener(this)
        container_set_length.setOnClickListener(this)
        container_sign_out.setOnClickListener(this)
        container_rod.setOnClickListener(this)
        container_slab.setOnClickListener(this)
        btn_select_mode.setOnClickListener(this)

        checkInternetAndGpsConnection()

        registerNetworkStateReceiver()
        listenNetworkStatus()

        container_main_menu.setBackgroundColor(ContextCompat.getColor(this, R.color.lightGreen))
        tv_email.text = sessionManager.userEmail

        checkPermissions(false, true)

        if (sessionManager.mode == "rod") {
            changeType(false)
        } else {
            changeType(false)
        }
    }

    override fun onResume() {
        super.onResume()
        val length = sessionManager.length
        if (length != 0f) {
            tv_show_old_length.text = """${getString(R.string.len)}$length""" + " m"
        } else {
            tv_show_old_length.text = getString(R.string.length) + " m"
        }
        updateLocation()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_open_camera -> openCamera()
            R.id.btn_open_gallery -> openGallery()
            R.id.drawerHomeBtn -> drawer_layout.openDrawer(GravityCompat.START)
            R.id.btn_info -> openActivity(MenuItem.INFO)
            R.id.container_images -> openActivity(MenuItem.IMAGES)
            R.id.container_info -> openActivity(MenuItem.INFO)
            R.id.container_main_menu -> openActivity(MenuItem.MAIN)
            R.id.container_map -> openActivity(MenuItem.MAP)
            R.id.container_set_length -> openActivity(MenuItem.LENGTH)
            R.id.tv_show_old_length -> openActivity(MenuItem.LENGTH)
            R.id.container_sign_out -> signOut()
            R.id.btn_select_mode -> showSelectorPopUp()
            R.id.container_rod -> changeType(false)
            R.id.container_slab -> changeType(true)
        }
    }

    private fun registerNetworkStateReceiver() {
        val intentFiler = IntentFilter()
        intentFiler.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkChangeReceiver, intentFiler)
    }

    private fun changeType(toSlab: Boolean) {
        if (toSlab) {
            btn_select?.animateSlide(300L,0f,(btn_select.width).toFloat(),1.0f)
            //tv_selected?.animateTextChange(200L, "Papírlap")
            tv_selected.text = "Papírlap"
            tv_slab?.fadeOut(300L)?.subscribe()
            tv_rod?.fadeIn(300L)?.subscribe()
            sessionManager.mode = "slab"
        } else {
            //tv_selected?.animateTextChange(200L, "Méterrúd")
            tv_selected.text = "Méterrúd"
            btn_select?.animateSlide(300L,0f,0f,1.0f)
            tv_rod?.fadeOut(300L)?.subscribe()
            tv_slab?.fadeIn(300L)?.subscribe()
            sessionManager.mode = "rod"
        }
    }

    private fun listenNetworkStatus() {
        eventBusDisposable = EventBus.instance.networkStatus
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ networkStatus ->
                when (networkStatus.state) {
                    NetworkStatus.State.ONLINE -> {
                        btn_internet.setImageResource(R.drawable.ic_wifi_on)
                    }
                    NetworkStatus.State.OFFLINE -> {
                        btn_internet.setImageResource(R.drawable.ic_wifi_off)
                    }
                }
            }, {
                it.printStackTrace()
            })
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

    private fun checkInternetAndGpsConnection() {
        if (Util.isNetworkAvailable(this)) {
            btn_internet.setImageResource(R.drawable.ic_wifi_on)
        } else {
            btn_internet.setImageResource(R.drawable.ic_wifi_off)
        }

        if (locationPermGiven() && Util.lat != null && Util.long != null) {
            btn_gps.setImageResource(R.drawable.ic_gps_on)
        } else {
            btn_gps.setImageResource(R.drawable.ic_gps_off)
        }
    }

    override fun successfulUpload(imageUpload: ResponseImageUpload, path: String?) {
        imageUpload.image?.let {
            if (it.isNotEmpty()) {
                val intent = Intent(this, UploadFinishedActivity::class.java)
                val responses = arrayListOf<ResponseImageUpload>()
                val pathList = arrayListOf<String>()
                responses.add(imageUpload)
                path?.let { pathList.add(path) }
                UploadFinishedActivity.responseList = responses
                intent.putStringArrayListExtra(ConfigInfo.PATH, pathList)
                startActivity(intent)
            } else {
                showToast("A szerver nem tudta feldolgozni a kérést, kérlek készíts másik képek.")
            }
        } ?: run {
            showToast("A szerver nem tudta feldolgozni a kérést, kérlek készíts másik képek.")
        }
    }

    override fun negativeButtonClicked() {}

    override fun positiveButtonClicked() {
        presenter.saveLocalImageItem()
    }

    @SuppressLint("MissingPermission")
    private fun updateLocation() {
        if (locationPermGiven()) {
            locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            locationListener = AgroLocationListener(applicationContext, this)
            val providers = locationManager?.getProviders(true)
            var bestLocation: Location? = null
            if (providers != null) {
                for (provider in providers) {
                    val loc: Location? = locationManager?.getLastKnownLocation(provider) ?: continue
                    if (bestLocation == null || loc!!.accuracy < bestLocation.accuracy) {
                        bestLocation = loc
                    }
                }
            }
            val lastKnown = locationManager?.getLastKnownLocation((LocationManager.GPS_PROVIDER))
            if (lastKnown != null) {
                Util.lat = lastKnown.latitude
                Util.long = lastKnown.longitude
            } else {
                if (bestLocation != null) {
                    Util.lat = bestLocation.latitude
                    Util.long = bestLocation.longitude
                }
            }
            locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0f, locationListener)
            checkInternetAndGpsConnection()
        }
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
                    startActivity(MapActivity::class.java, Bundle(), true)
                }
            }
            MenuItem.INFO -> {
                if (MenuItem.INFO.tag != TAG) {
                    startActivity(InfoActivity::class.java, Bundle(), true)
                }
            }
        }
        drawer_layout.closeDrawers()
    }

    private fun openGallery() {
        if (cameraPermGiven() && storagePermGiven() && locationPermGiven()) {
            startCropImageActivity()
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

    private fun startCropImageActivity() {
        CropImage.activity()
            .setAspectRatio(640, 480)
            .setGuidelines(CropImageView.Guidelines.ON)
            .start(this)
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

    override fun locationUpdated() {
        checkInternetAndGpsConnection()
        locationManager?.removeUpdates(locationListener)
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
                            startCropImageActivity()
                        }
                    }
                    updateLocation()
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest>,
                    token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }
            }).check()
    }

    private fun showSelectorPopUp() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage("Válaszd ki a kijelölés módját!")
            .setPositiveButton("Méterrúd") { dialog, which ->
                btn_select_mode.text = "Méterrúd"
                sessionManager.mode = "rod"
            }
            .setNegativeButton("Papírlap") { dialog, which ->
                btn_select_mode.text = "Papírlap"
                sessionManager.mode = "slab"
            }
            .setCancelable(false)
            .show()
    }

    override fun onStop() {
        super.onStop()
        try {
            unregisterReceiver(networkChangeReceiver)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        eventBusDisposable?.dispose()
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
                    CropImage.activity(uri)
                        .setAspectRatio(640, 480)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(this)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                val resultPath = CropImage.getActivityResult(data)?.uri?.path
                val pickedImage = CropImage.getActivityResult(data)?.originalUri
                if (resultCode == Activity.RESULT_OK && resultPath != null) {
                    if (sessionManager.mode == "rod") {
                        val intent = Intent(this, RodSelectorActivity::class.java)
                        intent.putExtra(ConfigInfo.PATH, resultPath)

                        val options = BitmapFactory.Options()
                        options.inScaled = false
                        options.inJustDecodeBounds = false
                        val bmp = BitmapFactory.decodeFile(resultPath, options)

                        RodSelectorActivity.bitmap = bmp

                        startActivity(intent)
                    } else {
                        val defaultBitmap = BitmapFactory.decodeFile(resultPath)
                        val resizedBitmap = Bitmap.createScaledBitmap(defaultBitmap, 640, 480, true)
                        resizedBitmap?.let {
                            presenter.uploadImage(resultPath, BitmapUtils.bitmapToBase64(resizedBitmap), null, null)
                        }
                    }
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    showToast("Something went wrong. Please try again.")
                }
            }
        }
    }

}