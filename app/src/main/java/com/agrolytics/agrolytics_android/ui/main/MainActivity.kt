package com.agrolytics.agrolytics_android.ui.main

import android.Manifest
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import com.agrolytics.agrolytics_android.R
import com.agrolytics.agrolytics_android.base.BaseActivity
import com.agrolytics.agrolytics_android.database.tables.RoomModule
import com.agrolytics.agrolytics_android.networking.AppServer
import com.agrolytics.agrolytics_android.ui.cropper.CropperActivity
import com.agrolytics.agrolytics_android.ui.guide.GuideActivity
import com.agrolytics.agrolytics_android.ui.images.ImagesActivity
import com.agrolytics.agrolytics_android.ui.info.InfoActivity
import com.agrolytics.agrolytics_android.ui.login.LoginActivity
import com.agrolytics.agrolytics_android.ui.map.MapActivity
import com.agrolytics.agrolytics_android.utils.*
import com.agrolytics.agrolytics_android.utils.ConfigInfo.IMAGE_CAPTURE
import com.agrolytics.agrolytics_android.utils.ConfigInfo.IMAGE_BROWSE
import com.agrolytics.agrolytics_android.utils.Util.Companion.showParameterSettingsWindow
import com.agrolytics.agrolytics_android.utils.extensions.cameraPermGiven
import com.agrolytics.agrolytics_android.utils.extensions.locationPermGiven
import com.agrolytics.agrolytics_android.utils.extensions.storagePermGiven
import com.google.firebase.auth.FirebaseAuth
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import jp.wasabeef.blurry.Blurry
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.nav_bar.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.koin.android.ext.android.inject
import kotlin.system.exitProcess


class MainActivity : BaseActivity(), View.OnClickListener, MainScreen, BaseActivity.OnDialogActions {

    private val TAG = "MainActivity"

    private val presenter: MainPresenter by inject()
    private val appServer: AppServer by inject()
    private val sessionManager: SessionManager by inject()
    private val roomModule: RoomModule by inject()

    private var locationManager: LocationManager? = null
    private var locationListener: AgroLocationListener? = null

    private lateinit var imageUri: Uri

    /** FAB and animations*/
    private var fabClicked = false
    private val rotateOpen : Animation by lazy{ AnimationUtils.loadAnimation(this, R.anim.rotate_open) }
    private val rotateClose : Animation by lazy{ AnimationUtils.loadAnimation(this, R.anim.rotate_close) }
    private val fabSlideUpRight : Animation by lazy{ AnimationUtils.loadAnimation(this, R.anim.fab_slide_up_right)}
    private val fabSlideDownLeft : Animation by lazy{ AnimationUtils.loadAnimation(this, R.anim.fab_slide_down_left)}
    private val fabSlideUpLeft : Animation by lazy{ AnimationUtils.loadAnimation(this, R.anim.fab_slide_up_left)}
    private val fabSlideDownRight : Animation by lazy{ AnimationUtils.loadAnimation(this, R.anim.fab_slide_down_right)}
    private val grayToRedAnim = ValueAnimator()
    private val redToGrayAnim = ValueAnimator()

    /**Internet Handler*/
    /**
     * TODO:
     * -Add a broadcast receiver to check for connectivity changes. This will only be needed to update the icons
     * -Decrease periodicity of internetCheckHandler
     * -Use a combined version of internetCheckHandler and broadcast receiver
    */
    private val internetCheckHandler = Handler()
    private val internetCheckRunnable = Runnable{handleWifiGpsIcons()}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        presenter.addView(this)
        presenter.addInjections(arrayListOf(appServer, roomModule, sessionManager))
        presenter.setActivity(this)

        Detector.init(assets)
        initFabColorAnimators()

        menu_frame.setOnClickListener(this)
        rod_frame.setOnClickListener(this)
        images_frame.setOnClickListener(this)
        map_frame.setOnClickListener(this)

        cameraFab.setOnClickListener(this)
        browseFab.setOnClickListener(this)

        container_profile.setOnClickListener(this)
        container_guide.setOnClickListener(this)
        container_impressum.setOnClickListener(this)
        container_logout.setOnClickListener(this)

        checkInternetAndGpsConnection()
        
        internetCheckHandler.postDelayed(internetCheckRunnable, 0)

        mainFab.setOnClickListener { fabHandler() }

        checkPermissions(isCamera = false, isDefault = true)

    }

    private fun initFabColorAnimators(){
        val from = ContextCompat.getColor(this, R.color.red)
        val to = ContextCompat.getColor(this, R.color.darkGrey)

        redToGrayAnim.setIntValues(from, to)
        redToGrayAnim.setEvaluator(ArgbEvaluator())
        redToGrayAnim.addUpdateListener { valueAnimator ->
            mainFab.backgroundTintList = ColorStateList.valueOf(valueAnimator.animatedValue as Int)
        }
        redToGrayAnim.duration = 300

        grayToRedAnim.setIntValues(to, from)
        grayToRedAnim.setEvaluator(ArgbEvaluator())
        grayToRedAnim.addUpdateListener { valueAnimator ->
            mainFab.backgroundTintList = ColorStateList.valueOf(valueAnimator.animatedValue as Int)
        }
        grayToRedAnim.duration = 300
    }

    private fun fabHandler() {
        when (fabClicked) {
            true -> closeFab()
            false -> openFab()
        }
    }

    private fun openFab(){
        mainFab.startAnimation(rotateOpen)
        cameraFab.startAnimation(fabSlideUpRight)
        browseFab.startAnimation(fabSlideUpLeft)

        cameraFab.visibility = View.VISIBLE
        browseFab.visibility = View.VISIBLE

        cameraFab.isClickable = true
        browseFab.isClickable = true

        grayToRedAnim.start()
        fabClicked = true
    }

    private fun closeFab(){
        mainFab.startAnimation(rotateClose)
        cameraFab.startAnimation(fabSlideDownLeft)
        browseFab.startAnimation(fabSlideDownRight)

        cameraFab.visibility = View.GONE
        browseFab.visibility = View.GONE

        cameraFab.isClickable = false
        browseFab.isClickable = false

        redToGrayAnim.start()
        fabClicked = false
    }

    private fun handleWifiGpsIcons() {
        val connected = Util.isInternetAvailable()

        if(connected){
            wifi_icon.setImageResource(R.drawable.ic_wifi_on)
        }
        else {
            wifi_icon.setImageResource(R.drawable.ic_wifi_off)
        }

        internetCheckHandler.postDelayed(internetCheckRunnable, 2000)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.cameraFab -> openCamera()
            R.id.browseFab -> openGallery()

            R.id.menu_frame -> drawer_layout.openDrawer(GravityCompat.START)
            R.id.rod_frame -> openActivity(MenuItem.ROD)
            R.id.images_frame -> openActivity(MenuItem.IMAGES)
            R.id.gps_frame -> openActivity(MenuItem.MAP)

            R.id.container_profile -> {/*TODO*/}
            R.id.container_impressum -> openActivity(MenuItem.INFO)
            R.id.container_guide -> openActivity(MenuItem.GUIDE)
            R.id.container_logout -> signOut()
        }
    }

    private fun openActivity(menuItem: MenuItem) {
        when (menuItem) {
            MenuItem.INFO -> startActivity(InfoActivity::class.java, Bundle(), true)
            MenuItem.GUIDE -> startActivity(GuideActivity::class.java, Bundle(), true)
            MenuItem.IMAGES -> startActivity(ImagesActivity::class.java, Bundle(), true)
            MenuItem.MAP -> startActivity(MapActivity::class.java, Bundle(), true)
            MenuItem.ROD -> showParameterSettingsWindow(this, sessionManager, ::blur)
            MenuItem.MAIN -> startActivity(MainActivity::class.java, Bundle(), true)
        }
        drawer_layout.closeDrawers()
    }

    private fun openGallery() {
        if (cameraPermGiven() && storagePermGiven() && locationPermGiven()) {
            browseImageActivity()
        } else {
            checkPermissions(isCamera = false, isDefault = false)
        }
    }

    private fun openCamera() {
        if (cameraPermGiven() && storagePermGiven()) {
            startCameraActivity()
        } else {
            checkPermissions(isCamera = true, isDefault = false)
        }
    }

    private fun browseImageActivity() {
        intent = Intent(Intent.ACTION_GET_CONTENT).setType("image/*")
        startActivityForResult(intent, IMAGE_BROWSE)
    }

    private fun startCameraActivity() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera")
        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)!!
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        startActivityForResult(intent, IMAGE_CAPTURE)
    }

    private fun startCropperActivity(imgUri: Uri){
        val intent = Intent(this, CropperActivity::class.java)
        intent.putExtra("IMAGE", imgUri)
        startActivity(intent)
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
                            startCameraActivity()
                        } else {
                            browseImageActivity()
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

    private fun signOut() {
        FirebaseAuth.getInstance().signOut()
        sessionManager.clearSession()
        doAsync {
            roomModule.database?.clearAllTables()
            uiThread {
                startActivity(LoginActivity::class.java, Bundle(), false)
            }
        }
    }

    private fun checkInternetAndGpsConnection() {
        if (Util.isNetworkAvailable()) {
            wifi_icon.setImageResource(R.drawable.ic_wifi_on)

            //This is needed to retrieve user token when during app startup
            //auto-login was successful, but there was not internet connection.
            if (appServer.getUserToken() == null){
                val auth = FirebaseAuth.getInstance()
                val currentUser = auth.currentUser
                currentUser?.getIdToken(false)?.addOnSuccessListener { userToken ->
                    appServer.updateApiService(userToken.token)
                }?.addOnFailureListener {
                    //TODO
                }
            }

        } else {
            wifi_icon.setImageResource(R.drawable.ic_wifi_off)
        }

        if (locationPermGiven() && Util.lat != null && Util.long != null) {
            gps_icon.setImageResource(R.drawable.ic_gps_on)
        } else {
            gps_icon.setImageResource(R.drawable.ic_gps_off)
        }
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
                    val loc: Location = locationManager?.getLastKnownLocation(provider) ?: continue
                    if (bestLocation == null || loc.accuracy < bestLocation.accuracy) {
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

    override fun locationUpdated() {
        checkInternetAndGpsConnection()
        locationManager?.removeUpdates(locationListener)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            IMAGE_CAPTURE -> {
                try {
                    val thumbnail = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                    val uri = presenter.getImageUri(thumbnail)
                    Log.d("HGXQR", "MAIN ACTIVITY UNCROPPED bitmap height" + thumbnail.height)
                    startCropperActivity(uri)
                } catch (e: Exception) {
                    Log.d("Camera", "Failed to get camera image Uri: $e")
                    e.printStackTrace()
                }
            }
            IMAGE_BROWSE -> startCropperActivity(data!!.data!!)
            }
        }

    private fun blur(iRadius : Int){
        val wView = (findViewById<View>(android.R.id.content) as ViewGroup).getChildAt(0) as ViewGroup
        if (iRadius == 0){
            Blurry.delete(wView)
            yellow_bg.animate().alpha(0f).duration = 0
        }
        else {
            yellow_bg.animate().alpha(0.95f).duration = 250
            Blurry.with(this)
                .radius(iRadius)
                .sampling(8)
                .async()
                .animate(250)
                .onto(wView)
        }

    }

    override fun positiveButtonClicked() {}

    override fun negativeButtonClicked() {}

    override fun onBackPressed() {
        finish()
        exitProcess(0)
    }

    override fun onResume() {
        super.onResume()
        updateLocation()
    }

    override fun onPause() {
        super.onPause()
        closeFab()
    }
}