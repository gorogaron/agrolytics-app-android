package com.agrolytics.agrolytics_android.ui.main

import android.Manifest
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.AlertDialog
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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
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
import com.agrolytics.agrolytics_android.utils.ConfigInfo.CAMERA_CAPTURE
import com.agrolytics.agrolytics_android.utils.ConfigInfo.PICK_IMAGE
import com.agrolytics.agrolytics_android.utils.extensions.cameraPermGiven
import com.agrolytics.agrolytics_android.utils.extensions.locationPermGiven
import com.agrolytics.agrolytics_android.utils.extensions.storagePermGiven
import com.google.firebase.auth.FirebaseAuth
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.nav_bar.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.koin.android.ext.android.inject


class MainActivity : BaseActivity(), View.OnClickListener, MainScreen, BaseActivity.OnDialogActions {

    private val TAG = "MainActivity"

    private val presenter: MainPresenter by inject()
    private val appServer: AppServer by inject()
    private val sessionManager: SessionManager by inject()
    private val roomModule: RoomModule by inject()

    private var locationManager: LocationManager? = null
    private var locationListener: AgroLocationListener? = null

    private var imageUri: Uri? = null

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

        checkPermissions(false, true)

    }

    private fun initFabColorAnimators(){
        val from = ContextCompat.getColor(this, R.color.red)
        val to = ContextCompat.getColor(this, R.color.darkGrey)

        redToGrayAnim.setIntValues(from, to)
        redToGrayAnim.setEvaluator(ArgbEvaluator())
        redToGrayAnim.addUpdateListener { valueAnimator -> mainFab.backgroundTintList = ColorStateList.valueOf(valueAnimator.animatedValue as Int) }
        redToGrayAnim.duration = 300

        grayToRedAnim.setIntValues(to, from)
        grayToRedAnim.setEvaluator(ArgbEvaluator())
        grayToRedAnim.addUpdateListener { valueAnimator -> mainFab.backgroundTintList = ColorStateList.valueOf(valueAnimator.animatedValue as Int) }
        grayToRedAnim.duration = 300
    }

    private fun fabHandler() {
        if (fabClicked) {
            closeFab()
        }
        else {
            openFab()
        }
        fabClicked = !fabClicked
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

            R.id.rod_frame -> openActivity(MenuItem.LENGTH)
            R.id.images_frame -> openActivity(MenuItem.IMAGES)
            R.id.menu_frame -> drawer_layout.openDrawer(GravityCompat.START)
            R.id.gps_frame -> openActivity(MenuItem.MAP)

            R.id.container_profile -> {/*TODO*/}
            R.id.container_impressum -> openActivity(MenuItem.INFO)
            R.id.container_guide -> openActivity(MenuItem.GUIDE)
            R.id.container_logout -> signOut()
        }
    }

    private fun openActivity(menuItem: MenuItem) {
        when (menuItem) {
            MenuItem.LENGTH -> {
                if (MenuItem.LENGTH.tag != TAG) {
                    createRodDialog()
                    //startActivity(LengthActivity::class.java, Bundle(), true)
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
                //finish()
            }
        }
    }

    private fun checkInternetAndGpsConnection() {
        if (Util.isNetworkAvailable()) {
            wifi_icon.setImageResource(R.drawable.ic_wifi_on)

            //This is needed to retrieve user token when during app startup
            //auto-login was successful, but there was not internet connection.
            if (appServer.getUserToken() == null){
                var auth = FirebaseAuth.getInstance()
                var currentUser = auth.currentUser
                currentUser?.getIdToken(false)?.addOnSuccessListener { userToken ->
                    appServer.updateApiService(userToken.token)
                }?.addOnFailureListener { e ->
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

    override fun negativeButtonClicked() {
        //Do nothing
    }

    override fun positiveButtonClicked() {
        //Do nothing
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

    override fun locationUpdated() {
        checkInternetAndGpsConnection()
        locationManager?.removeUpdates(locationListener)
    }

    override fun onBackPressed() {
        finish()
        System.exit(0)
    }

    private fun startCropper(imgUri: Uri){
        val intent = Intent(this, CropperActivity::class.java)
        intent.putExtra("IMAGE", imgUri)
        startActivity(intent)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
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

    private fun createRodDialog() {
        blur(10)
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Adatok")
        val view = LayoutInflater.from(this).inflate(R.layout.rod_dialog, null, false)

        val et_length_rod = view.findViewById<EditText>(R.id.et_length_rod)
        val et_length_wood = view.findViewById<EditText>(R.id.et_wood_length)

        et_length_rod.setText(sessionManager.rodLength.toString())
        et_length_wood.setText(sessionManager.woodLength.toString())

        val spinner = view.findViewById<Spinner>(R.id.wood_type_spinner)
        val spinnerAdapter = ArrayAdapter.createFromResource(this, R.array.wood_types, android.R.layout.simple_spinner_item)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = spinnerAdapter

        builder.setView(view)
        builder.setPositiveButton("Ok") { dialog, which ->
            if (et_length_rod.text.isNotEmpty()) {
                sessionManager.rodLength = et_length_rod.text.toString().toFloat()
                sessionManager.woodType = spinner.selectedItem.toString()
                sessionManager.woodLength = et_length_wood.text.toString().toFloat()
            }
            Util.hideKeyboard(this, et_length_rod)
            blur(0)
        }

        builder.setCancelable(false)
        val dialog = builder.create()
        dialog.window!!.setBackgroundDrawableResource(R.drawable.parameter_dialog_bg)
        dialog.window.setDimAmount(0.0f)
        dialog.show()
    }

    fun blur(iRadius : Int){
        val wView = (findViewById<View>(android.R.id.content) as ViewGroup).getChildAt(0) as ViewGroup
        if (iRadius == 0){
            Blurry.delete(wView)
            yellow_bg.animate().alpha(0f).setDuration(0)
        }
        else {
            yellow_bg.animate().alpha(0.95f).setDuration(250)
            Blurry.with(this)
                .radius(iRadius)
                .sampling(8)
                .async()
                .animate(250)
                .onto(wView)
        }

    }

    override fun onResume() {
        super.onResume()
        updateLocation()
    }
}