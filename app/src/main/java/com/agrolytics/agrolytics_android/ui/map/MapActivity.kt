package com.agrolytics.agrolytics_android.ui.map

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import com.agrolytics.agrolytics_android.R
import com.agrolytics.agrolytics_android.data.DataClient
import com.agrolytics.agrolytics_android.data.local.tables.CachedImageItem
import com.agrolytics.agrolytics_android.ui.base.BaseActivity
import com.agrolytics.agrolytics_android.ui.images.ImagesActivity
import com.agrolytics.agrolytics_android.ui.info.InfoActivity
import com.agrolytics.agrolytics_android.ui.main.MainActivity
import com.agrolytics.agrolytics_android.types.ConfigInfo
import com.agrolytics.agrolytics_android.ui.map.utils.MarkerInfoBottomSheetDialog
import com.agrolytics.agrolytics_android.types.MenuItem
import com.agrolytics.agrolytics_android.utils.SessionManager
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.IconFactory
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import kotlinx.android.synthetic.main.activity_map.*
import kotlinx.android.synthetic.main.nav_bar.*
import org.koin.android.ext.android.inject


class MapActivity : BaseActivity(), MapScreen, View.OnClickListener {

    private val TAG = "MapActivity"

    private val sessionManager: SessionManager by inject()
    private val dataClient: DataClient by inject()

    private lateinit var mapboxMap: MapboxMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        //presenter.addView(this)
        //presenter.addInjections(arrayListOf(sessionManager, dataClient))

        btn_back.setOnClickListener { onBackPressed() }

        setUpMap()

        container_profile.setOnClickListener(this)
        container_guide.setOnClickListener(this)
        container_impressum.setOnClickListener(this)
        container_logout.setOnClickListener(this)

    }

    private fun setUpMap() {
        Mapbox.getInstance(this, ConfigInfo.MAP_BOX_KEY)
        mapView.getMapAsync { map ->
            mapboxMap = map
            mapboxMap.setStyle(Style.MAPBOX_STREETS) {
                mapboxMap = map

                mapboxMap.clear()

                //presenter.getAllUploadedImage()
                //presenter.getAllLocalImage()

                map.setOnMarkerClickListener {
                    val id = it.id
                    //presenter.getItemFromList(id.toInt())
                    true
                }
            }
        }
    }

    override fun showDetails(imageItem: CachedImageItem) {
        val ins = MarkerInfoBottomSheetDialog.instance()
        ins.initData(imageItem)
        ins.show(supportFragmentManager, "TAG")
    }

    override fun loadImages(images: ArrayList<CachedImageItem>, isOnline: Boolean) {
        val latLngBounds = LatLngBounds.Builder()

        var latLng = LatLng()
        for (mMarker in images) {
            latLng = LatLng(mMarker.location.latitude, mMarker.location.longitude)
            latLngBounds.include(latLng)

            val options = MarkerOptions()
            options.position(latLng)
            options.title = mMarker.timestamp.toString()
            options.marker.id = images.indexOf(mMarker).toLong()
            if (!isOnline) {
                val iconFactory = IconFactory.getInstance(this)
                //val iconDrawable = ContextCompat.getDrawable(this, R.drawable.ic_location)
                val bitmap = BitmapFactory.decodeResource(resources, R.drawable.gray_poi)
                val icon = iconFactory.fromBitmap(bitmap)

                options.icon = icon
            }
            mapboxMap.addMarker(options)

            try {
                mapboxMap.easeCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds.build(), 50))
            } catch (e: Exception) {
                mapboxMap.easeCamera(CameraUpdateFactory.newLatLng(latLng))
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.container_profile -> {/*TODO*/}
            R.id.container_impressum -> openActivity(MenuItem.INFO)
            R.id.container_guide -> openActivity(MenuItem.GUIDE)
            R.id.container_logout -> {/*TODO*/}
        }
    }

    private fun openActivity(menuItem: MenuItem) {
        when (menuItem) {
            MenuItem.MAIN -> {
                if (MenuItem.MAIN.tag != TAG) {
                    startActivity(MainActivity::class.java, Bundle(), false)
                }
            }
            MenuItem.IMAGES -> {
                if (MenuItem.IMAGES.tag != TAG) {
                    startActivity(ImagesActivity::class.java, Bundle(), false)
                }
            }
            MenuItem.MAP -> {
                if (MenuItem.MAP.tag != TAG) {
                    startActivity(MapActivity::class.java, Bundle(), false)
                }
            }
            MenuItem.INFO -> {
                if (MenuItem.INFO.tag != TAG) {
                    startActivity(InfoActivity::class.java, Bundle(), false)
                }
            }
        }
        drawer_layout.closeDrawers()
    }

}