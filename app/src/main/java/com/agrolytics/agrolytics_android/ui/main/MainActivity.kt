package com.agrolytics.agrolytics_android.ui.main

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentSender
import android.content.res.ColorStateList
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.agrolytics.agrolytics_android.AgrolyticsApp
import com.agrolytics.agrolytics_android.R
import com.agrolytics.agrolytics_android.data.DataClient
import com.agrolytics.agrolytics_android.ui.base.BaseActivity
import com.agrolytics.agrolytics_android.network.AppServer
import com.agrolytics.agrolytics_android.types.ConfigInfo
import com.agrolytics.agrolytics_android.types.ConfigInfo.GPS_TURN_ON_REQUEST
import com.agrolytics.agrolytics_android.types.MenuItem
import com.agrolytics.agrolytics_android.ui.guide.GuideActivity
import com.agrolytics.agrolytics_android.ui.images.ImagesActivity
import com.agrolytics.agrolytics_android.ui.info.InfoActivity
import com.agrolytics.agrolytics_android.ui.login.LoginActivity
import com.agrolytics.agrolytics_android.ui.map.MapActivity
import com.agrolytics.agrolytics_android.ui.measurement.MeasurementManager
import com.agrolytics.agrolytics_android.ui.measurement.utils.SessionRecyclerViewAdapter
import com.agrolytics.agrolytics_android.utils.*
import com.agrolytics.agrolytics_android.utils.Util.Companion.showParameterSettingsWindow
import com.agrolytics.agrolytics_android.utils.permissions.locationPermGiven
import com.agrolytics.agrolytics_android.utils.permissions.requestForAllPermissions
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import jp.wasabeef.blurry.Blurry
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.recycler_view
import kotlinx.android.synthetic.main.nav_bar.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread
import org.koin.android.ext.android.inject
import org.koin.core.component.KoinApiExtension
import kotlin.system.exitProcess


class MainActivity : BaseActivity(), View.OnClickListener {

    private val sessionManager: SessionManager by inject()
    private val dataClient: DataClient by inject()

    private lateinit var fusedLocationClient: FusedLocationProviderClient

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

    /**Recycler view*/
    lateinit var recyclerViewAdapter : SessionRecyclerViewAdapter
    lateinit var recyclerViewLayoutManager : LinearLayoutManager
    lateinit var viewModel : MainViewModel

    @KoinApiExtension
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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

        show_full_session.setOnClickListener(this)
        session_add.setOnClickListener(this)
        session_location.setOnClickListener(this)


        connectionLiveData.observe(this, Observer {
            if(it){
                wifi_icon.setImageResource(R.drawable.ic_wifi_on)
            }
            else {
                wifi_icon.setImageResource(R.drawable.ic_wifi_off)
            }
        })

        mainFab.setOnClickListener { fabHandler() }
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        viewModel.listenForFirebaseUpdates()
        viewModel.getLastMeasurementItems()
        AgrolyticsApp.databaseChanged.observe(this, Observer {
            viewModel.getLastMeasurementItems()
        })
        viewModel.lastMeasurementItems.observe(this, Observer {
            if (viewModel.lastMeasurementItems.value != null && viewModel.lastMeasurementItems.value!!.size > 0) {
                nested_scrollview.visibility = View.VISIBLE
                session_add.visibility = View.VISIBLE
                session_location.visibility = View.VISIBLE
                no_measurement_text.visibility = View.GONE
                val lastMeasurementItems = ArrayList(viewModel.lastMeasurementItems.value!!.take(3))
                if (!::recyclerViewAdapter.isInitialized) {
                    recyclerViewAdapter = SessionRecyclerViewAdapter(this@MainActivity, lastMeasurementItems)
                    recyclerViewLayoutManager = LinearLayoutManager(this@MainActivity)
                    recycler_view.layoutManager = recyclerViewLayoutManager
                    recycler_view.adapter = recyclerViewAdapter
                }
                else {
                    recyclerViewAdapter.itemList = lastMeasurementItems
                    recyclerViewAdapter.notifyDataSetChanged()
                }
            }
            else {
                nested_scrollview.visibility = View.GONE
                session_add.visibility = View.GONE
                session_location.visibility = View.GONE
                no_measurement_text.visibility = View.VISIBLE
            }

        })

        setupPermissions()

    }

    private fun setupPermissions(){
        val permissionListener = object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                if (report.deniedPermissionResponses.size != 0) {
                    toast(getString(R.string.permission_error))
                    finishAndRemoveTask()
                }
                else {
                    //Ha meg van adva minden engedély, kezdjük a GPS lekérdezést
                    fusedLocationClient = LocationServices.getFusedLocationProviderClient(this@MainActivity)
                    setupLocationUpdates()
                }
            }

            override fun onPermissionRationaleShouldBeShown(permissions: List<PermissionRequest>, token: PermissionToken) {
                token.continuePermissionRequest()
            }
        }
        requestForAllPermissions(this, permissionListener)
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

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.cameraFab -> MeasurementManager.startNewMeasurementSession(this, MeasurementManager.ImagePickerID.ID_CAMERA)
            R.id.browseFab -> MeasurementManager.startNewMeasurementSession(this, MeasurementManager.ImagePickerID.ID_BROWSER)

            R.id.menu_frame -> drawer_layout.openDrawer(GravityCompat.START)
            R.id.rod_frame -> showParameterSettingsWindow(this, sessionManager, ::blur)
            R.id.images_frame -> openActivity(MenuItem.IMAGES)
            R.id.map_frame -> openActivity(MenuItem.MAP)

            R.id.container_profile -> {/*TODO*/}
            R.id.container_guide -> openActivity(MenuItem.GUIDE)
            R.id.container_impressum -> openActivity(MenuItem.INFO)
            R.id.container_logout -> signOut()

            R.id.show_full_session -> MeasurementManager.showSession(this, viewModel.lastSessionId.value)
            R.id.session_add -> MeasurementManager.addNewMeasurementForSession(this, viewModel.lastSessionId.value!!)
            R.id.session_location -> MapActivity.openMapForSession(this, viewModel.lastSessionId.value!!)
        }
    }

    private fun openActivity(menuItem: MenuItem) {
        when (menuItem) {
            MenuItem.INFO -> startActivity(InfoActivity::class.java, Bundle(), true)
            MenuItem.GUIDE -> startActivity(GuideActivity::class.java, Bundle(), true)
            MenuItem.IMAGES -> startActivity(ImagesActivity::class.java, Bundle(), true)
            MenuItem.MAP -> MapActivity.openMapForAllImages(this)
            MenuItem.MAIN -> startActivity(MainActivity::class.java, Bundle(), true)
        }
        drawer_layout.closeDrawers()
    }

    private fun signOut() {
        viewModel.stopFirebaseUpdateListener()
        FirebaseAuth.getInstance().signOut()
        sessionManager.clearSession()
        doAsync {
            dataClient.local.clearDatabase()
            uiThread {
                startActivity(LoginActivity::class.java, Bundle(), false)
                finish()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun setupLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = 10000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult?) {
                    locationResult ?: return
                    //TODO: Miért lista van itt?
                    gps_icon.setImageResource(R.drawable.ic_gps_on)
                    val location = locationResult.locations[0]
                    Util.lat = location.latitude
                    Util.long = location.longitude
                    Log.d("LOCATION", "Location update receive")
                }

                override fun onLocationAvailability(p0: LocationAvailability) {
                    super.onLocationAvailability(p0)
                    if (!p0.isLocationAvailable) {
                        gps_icon.setImageResource(R.drawable.ic_gps_off)
                        Util.lat = null
                        Util.long = null
                    }
                    Log.d("LOCATION", "Location availibility updated")
                }
            }
            fusedLocationClient.requestLocationUpdates(locationRequest,
                    locationCallback,
                    Looper.getMainLooper())
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException){
                try {
                    //Megkérjük a user-t, hogy kapcsolja be a GPS-t
                    exception.startResolutionForResult(this@MainActivity, GPS_TURN_ON_REQUEST)
                } catch (sendEx: IntentSender.SendIntentException){
                    //TODO: Kezeljük a hibát
                }
            }
        }
    }

    private fun blur(iRadius : Int){
        val wView = (findViewById<View>(android.R.id.content) as ViewGroup).getChildAt(0) as ViewGroup
        if (iRadius == 0){
            Blurry.delete(wView)
            yellow_bg.animate().alpha(0f).duration = 0
        }
        else {
            yellow_bg.animate().alpha(0.5f).duration = 250
            Blurry.with(this)
                .radius(iRadius)
                .sampling(8)
                .async()
                .animate(250)
                .onto(wView)
        }
    }

    override fun onBackPressed() {
        finish()
        exitProcess(0)
    }

    override fun onResume() {
        super.onResume()
        viewModel.getLastMeasurementItems()
    }

    override fun onPause() {
        super.onPause()
        if (fabClicked)
        {
            closeFab()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.stopFirebaseUpdateListener()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (resultCode != RESULT_OK) {
            if (requestCode == ConfigInfo.IMAGE_CAPTURE || requestCode == ConfigInfo.IMAGE_BROWSE) {
                MeasurementManager.currentSessionId = 0
            }
        }
        if (resultCode == RESULT_CANCELED && requestCode == GPS_TURN_ON_REQUEST) {
            setupLocationUpdates()
            toast(getString(R.string.gps_off_message))
        }
    }
}