package com.agrolytics.agrolytics_android.ui.map

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.agrolytics.agrolytics_android.R
import com.agrolytics.agrolytics_android.data.DataClient
import com.agrolytics.agrolytics_android.data.local.tables.BaseImageItem
import com.agrolytics.agrolytics_android.data.local.tables.CachedImageItem
import com.agrolytics.agrolytics_android.data.local.tables.ProcessedImageItem
import com.agrolytics.agrolytics_android.types.ConfigInfo
import com.agrolytics.agrolytics_android.ui.base.BaseActivity
import com.agrolytics.agrolytics_android.utils.Util
import com.agrolytics.agrolytics_android.utils.Util.Companion.round
import com.github.chrisbanes.photoview.PhotoView
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.expressions.Expression.zoom
import kotlinx.android.synthetic.main.activity_map.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.java.KoinJavaComponent.inject


class MapActivity : BaseActivity() {

    private lateinit var mapboxMap: MapboxMap


    companion object : KoinComponent{
        var ImageItemList = ArrayList<BaseImageItem>()
        private val dataClient: DataClient by inject()

        fun openMapForSession(context : BaseActivity, sessionId : Long) {
            doAsync {
                val itemList = ArrayList<BaseImageItem>()
                itemList.addAll(dataClient.local.processed.getBySessionId(sessionId))
                itemList.addAll(dataClient.local.unprocessed.getBySessionId(sessionId))
                itemList.addAll(dataClient.local.cache.getBySessionId(sessionId))
                MapActivity.ImageItemList = itemList
                uiThread {
                    context.startActivity(MapActivity::class.java, Bundle(), true)
                }
            }
        }

        fun openMapForAllImages(context : BaseActivity) {
            doAsync {
                val itemList = ArrayList<BaseImageItem>()
                itemList.addAll(dataClient.local.processed.getAll())
                itemList.addAll(dataClient.local.unprocessed.getAll())
                itemList.addAll(dataClient.local.cache.getAll())
                MapActivity.ImageItemList = itemList
                uiThread {
                    context.startActivity(MapActivity::class.java, Bundle(), true)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, ConfigInfo.MAP_BOX_KEY)
        setContentView(R.layout.activity_map)


        btn_back.setOnClickListener { onBackPressed() }

        setUpMap()
    }

    private fun setUpMap() {
        mapView.getMapAsync { map ->
            mapboxMap = map
            mapboxMap.setStyle(Style.MAPBOX_STREETS)

            val locations = setupMarkers()
            moveCamera(locations)

            mapboxMap.setOnMarkerClickListener { marker ->
                val timestamp = marker.title
                val imageItemIndex = ImageItemList.indexOfFirst { it.timestamp.toString() == timestamp }
                val imageItem = ImageItemList[imageItemIndex]
                showImageItemDetails(imageItem)
                true
            }
        }
    }

    private fun moveCamera(locations : List<LatLng>) {
        if (locations.size > 1) {
            val locationBound = LatLngBounds.Builder()
            for (location in locations) {
                locationBound.include(location)
            }
            mapboxMap.easeCamera(CameraUpdateFactory.newLatLngBounds(locationBound.build(), 50), 1000)
        }
        else if (locations.size == 1) {
            val position = CameraPosition.Builder().target(locations[0]).zoom(10.0).build()
            mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 1000);
        }
    }

    private fun setupMarkers() : List<LatLng>{
        val locations = ArrayList<LatLng>()
        var numOfNullLocations = 0
        for (imageItem in ImageItemList) {
            val itemLocation = LatLng(imageItem.location.latitude, imageItem.location.longitude)
            if (itemLocation.latitude != 0.0 && itemLocation.longitude != 0.0) {
                mapboxMap.addMarker(MarkerOptions().position(itemLocation).title(imageItem.timestamp.toString()))
                locations.add(itemLocation)
            }
            else {
                numOfNullLocations += 1
            }
        }
        img_without_coordinate.text = getString(R.string.images_without_coordinates, numOfNullLocations)
        return locations
    }

    //TODO: Egyesíteni ezt a függvényt a SessionRecyclerViewAdapter-ben lévővel
    private fun showImageItemDetails(imageItem: BaseImageItem) {
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(true)
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_image_item, null, false)
        builder.setView(view)
        val dialog = builder.create()

        view.findViewById<TextView>(R.id.volume).text = when (imageItem.getItemType()) {
            ConfigInfo.IMAGE_ITEM_TYPE.CACHED -> (imageItem as CachedImageItem).woodVolume.round(2).toString()
            ConfigInfo.IMAGE_ITEM_TYPE.PROCESSED -> (imageItem as ProcessedImageItem).woodVolume.round(2).toString()
            ConfigInfo.IMAGE_ITEM_TYPE.UNPROCESSED -> "Mérésre vár"
        }
        view.findViewById<TextView>(R.id.length).text = imageItem.woodLength.toString()
        view.findViewById<TextView>(R.id.species).text = imageItem.woodType
        view.findViewById<TextView>(R.id.date).text = Util.getFormattedDateTime(imageItem.timestamp)
        view.findViewById<PhotoView>(R.id.image).setImageBitmap(imageItem.image)
        view.findViewById<ImageView>(R.id.btn_delete).visibility = View.GONE
        dialog.window!!.setBackgroundDrawableResource(R.drawable.bg_white_round)
        dialog.show()
    }

}