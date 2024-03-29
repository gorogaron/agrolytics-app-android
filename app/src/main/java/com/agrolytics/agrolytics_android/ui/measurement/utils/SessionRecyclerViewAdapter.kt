package com.agrolytics.agrolytics_android.ui.measurement.utils

import android.app.AlertDialog
import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Delete
import androidx.work.*
import com.agrolytics.agrolytics_android.AgrolyticsApp
import com.agrolytics.agrolytics_android.R
import com.agrolytics.agrolytics_android.data.DataClient
import com.agrolytics.agrolytics_android.data.firebase.model.ImageDirectory
import com.agrolytics.agrolytics_android.data.local.tables.BaseImageItem
import com.agrolytics.agrolytics_android.data.local.tables.CachedImageItem
import com.agrolytics.agrolytics_android.data.local.tables.ProcessedImageItem
import com.agrolytics.agrolytics_android.data.local.tables.UnprocessedImageItem
import com.agrolytics.agrolytics_android.types.ConfigInfo
import com.agrolytics.agrolytics_android.ui.base.BaseActivity
import com.agrolytics.agrolytics_android.ui.measurement.MeasurementManager
import com.agrolytics.agrolytics_android.ui.measurement.activity.RodSelectorActivity
import com.agrolytics.agrolytics_android.utils.ImageUtils
import com.agrolytics.agrolytics_android.utils.SessionManager
import com.agrolytics.agrolytics_android.utils.Util.Companion.cubicMeter
import com.agrolytics.agrolytics_android.utils.Util.Companion.getFormattedDateTime
import com.agrolytics.agrolytics_android.utils.Util.Companion.round
import com.github.chrisbanes.photoview.PhotoView
import kotlinx.android.synthetic.main.recycler_view_measurement_item.view.*
import kotlinx.coroutines.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import org.koin.core.component.KoinComponent
import org.koin.core.component.bind
import org.koin.core.component.inject
import retrofit2.awaitResponse
import kotlin.collections.ArrayList

class SessionRecyclerViewAdapter(var activity : BaseActivity, var itemList : ArrayList<BaseImageItem>) : RecyclerView.Adapter<SessionRecyclerViewAdapter.SessionViewHolder>(), KoinComponent {

    private val dataClient: DataClient by inject()
    private val workManager = WorkManager.getInstance(activity.application)
    private lateinit var itemStateList : ArrayList<ConfigInfo.IMAGE_ITEM_STATE>

    private val stateUpdateObserver = object : RecyclerView.AdapterDataObserver() {
        override fun onChanged() {
            super.onChanged()
            itemStateList = ArrayList()
            for ((index, imageItem) in itemList.withIndex()) {
                when (imageItem.getItemType()) {
                    ConfigInfo.IMAGE_ITEM_TYPE.CACHED -> {
                        itemStateList.add(ConfigInfo.IMAGE_ITEM_STATE.UPLOADED)

                        //Csak cached itemnél kell delete worker observer, mert a többinél egyből megtörténik a törlés a local DB-ből
                        startObserverForDeletedImageItem(imageItem, index)
                    }
                    ConfigInfo.IMAGE_ITEM_TYPE.UNPROCESSED -> itemStateList.add(ConfigInfo.IMAGE_ITEM_STATE.WAITING_FOR_PROCESSING)
                    ConfigInfo.IMAGE_ITEM_TYPE.PROCESSED -> {
                        itemStateList.add(ConfigInfo.IMAGE_ITEM_STATE.UNDEFINED)

                        //Coroutine indítása UI szálon, mert observereket nem lehet háttérszálon kezelni
                        GlobalScope.launch(Dispatchers.Main) {
                            startObserverForProcessedImageItem(imageItem, index)
                        }
                    }
                }
            }
        }
    }

    init {
        registerAdapterDataObserver(stateUpdateObserver)
        stateUpdateObserver.onChanged()
    }

    inner class SessionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        var imageView = itemView.image
        var volumeTextView = itemView.volume_text
        var dateTextView = itemView.date_text
        var stateTextView = itemView.state
        var uploadButton = itemView.upload_button

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val clickedImageItem = itemList[bindingAdapterPosition]
            showImageItemDetails(clickedImageItem, bindingAdapterPosition)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_measurement_item, parent, false)
        return SessionViewHolder(view)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
        val imageItem = itemList[position]
        holder.imageView.setImageBitmap(imageItem.image)
        holder.dateTextView.text = getFormattedDateTime(imageItem.timestamp)
        when (imageItem.getItemType()) {
            ConfigInfo.IMAGE_ITEM_TYPE.PROCESSED -> {
                val processedImageItem = imageItem as ProcessedImageItem
                holder.volumeTextView.visibility = View.VISIBLE
                holder.uploadButton.visibility = View.GONE
                holder.volumeTextView.text = cubicMeter(activity, processedImageItem.woodVolume + processedImageItem.addedWoodVolume)
                //Ha volt manuális korrekció, a térfogat text színe legyen piros, egyébként sötétszürke
                if (processedImageItem.addedWoodVolume > 0) {
                    holder.volumeTextView.setTextColor(ContextCompat.getColor(activity,R.color.red))
                }
                else {
                    holder.volumeTextView.setTextColor(ContextCompat.getColor(activity,R.color.textColor))
                }
            }
            ConfigInfo.IMAGE_ITEM_TYPE.CACHED -> {
                val cachedImageItem = imageItem as CachedImageItem
                holder.volumeTextView.visibility = View.VISIBLE
                holder.uploadButton.visibility = View.GONE
                holder.volumeTextView.text = cubicMeter(activity, cachedImageItem.woodVolume + cachedImageItem.addedWoodVolume)
                //Ha volt manuális korrekció, a térfogat text színe legyen piros, egyébként sötétszürke
                if (cachedImageItem.addedWoodVolume > 0) {
                    holder.volumeTextView.setTextColor(ContextCompat.getColor(activity,R.color.red))
                }
                else {
                    holder.volumeTextView.setTextColor(ContextCompat.getColor(activity,R.color.textColor))
                }
            }
            ConfigInfo.IMAGE_ITEM_TYPE.UNPROCESSED -> {
                holder.volumeTextView.visibility = View.GONE
                holder.uploadButton.visibility = View.VISIBLE
                holder.uploadButton.setOnClickListener {
                    processUnprocessedImageItem(itemList[position] as UnprocessedImageItem)
                }
            }
        }

        holder.stateTextView.text = when(itemStateList[position]) {
            ConfigInfo.IMAGE_ITEM_STATE.BEING_UPLOADED -> {activity.getString(R.string.item_state_being_uploaded)}
            ConfigInfo.IMAGE_ITEM_STATE.READY_TO_UPLOAD -> {activity.getString(R.string.item_state_ready_to_upload)}
            ConfigInfo.IMAGE_ITEM_STATE.UPLOADED -> {activity.getString(R.string.item_state_uploaded)}
            ConfigInfo.IMAGE_ITEM_STATE.WAITING_FOR_PROCESSING -> {activity.getString(R.string.item_state_waiting_for_processing)}
            ConfigInfo.IMAGE_ITEM_STATE.UNDEFINED -> {""}
            ConfigInfo.IMAGE_ITEM_STATE.BEING_DELETED -> {activity.getString(R.string.item_state_being_deleted)}
        }
    }

    fun showImageItemDetails(imageItem: BaseImageItem, index: Int) {
        val builder = AlertDialog.Builder(activity)
        builder.setCancelable(true)
        val view = LayoutInflater.from(activity).inflate(R.layout.dialog_image_item, null, false)
        builder.setView(view)
        val dialog = builder.create()

        when (imageItem.getItemType()) {
            ConfigInfo.IMAGE_ITEM_TYPE.CACHED -> {
                imageItem as CachedImageItem
                view.findViewById<TextView>(R.id.volume).text = (imageItem.woodVolume + imageItem.addedWoodVolume).round(2).toString()
                view.findViewById<TextView>(R.id.correction).text = imageItem.addedWoodVolume.round(2).toString()
                view.findViewById<TextView>(R.id.justification).text = imageItem.addedWoodVolumeJustification
            }
            ConfigInfo.IMAGE_ITEM_TYPE.PROCESSED -> {
                imageItem as ProcessedImageItem
                view.findViewById<TextView>(R.id.volume).text = (imageItem.woodVolume + imageItem.addedWoodVolume).round(2).toString()
                view.findViewById<TextView>(R.id.correction).text = imageItem.addedWoodVolume.round(2).toString()
                view.findViewById<TextView>(R.id.justification).text = imageItem.addedWoodVolumeJustification
            }
            ConfigInfo.IMAGE_ITEM_TYPE.UNPROCESSED -> {
                view.findViewById<TextView>(R.id.volume).text = activity.getString(R.string.item_state_waiting_for_processing)
                //TODO: correction és justification elrejtése
            }
        }

        view.findViewById<TextView>(R.id.length).text = imageItem.woodLength.toString()
        view.findViewById<TextView>(R.id.species).text = imageItem.woodType
        view.findViewById<TextView>(R.id.date).text = getFormattedDateTime(imageItem.timestamp)
        view.findViewById<PhotoView>(R.id.image).setImageBitmap(imageItem.image)
        view.findViewById<ImageView>(R.id.btn_delete).setOnClickListener{
            if (itemStateList[index] == ConfigInfo.IMAGE_ITEM_STATE.BEING_UPLOADED ||
                itemStateList[index] == ConfigInfo.IMAGE_ITEM_STATE.UNDEFINED) {
                activity.toast(activity.getString(R.string.upload_delete_error))
            }
            else {
                showDeleteConfirmation(dialog, imageItem, index)
            }
        }
        dialog.window!!.setBackgroundDrawableResource(R.drawable.bg_white_round)
        dialog.show()
    }

    private fun showDeleteConfirmation(parentDialog: AlertDialog, imageItem : BaseImageItem, index: Int) {
        val childView = activity.layoutInflater.inflate(R.layout.confirm_delete, null)
        activity.mContentView.addView(childView)
        childView.layoutParams.height = MATCH_PARENT
        childView.layoutParams.width = MATCH_PARENT
        activity.findViewById<Button>(R.id.confirm_delete_btn).setOnClickListener {
            GlobalScope.launch(Dispatchers.IO) {
                when (imageItem.getItemType()) {
                    ConfigInfo.IMAGE_ITEM_TYPE.CACHED -> {
                        val inputData = Data.Builder()
                            .putString(ConfigInfo.CACHED_IMAGE_ITEM_FIRESTORE_ID, (imageItem as CachedImageItem).firestoreId)
                            .build()

                        val uploadRequest = OneTimeWorkRequestBuilder<DeleteWorker>()
                            .addTag("DELETE" + imageItem.timestamp.toString())
                            .setInputData(inputData)
                            .build()

                        workManager.enqueueUniqueWork(imageItem.timestamp.toString(), ExistingWorkPolicy.KEEP, uploadRequest)
                    }
                    ConfigInfo.IMAGE_ITEM_TYPE.PROCESSED -> {
                        dataClient.local.processed.delete(imageItem as ProcessedImageItem)
                        MeasurementManager.recentlyAddedItemTimestamps.remove(imageItem.timestamp)   //Ha egy aktív mérés folyik, töröljük ott is a képet
                    }
                    ConfigInfo.IMAGE_ITEM_TYPE.UNPROCESSED -> {
                        dataClient.local.unprocessed.delete(imageItem as UnprocessedImageItem)
                        MeasurementManager.recentlyAddedItemTimestamps.remove(imageItem.timestamp)   //Ha egy aktív mérés folyik, töröljük ott is a képet
                    }
                }
                withContext(Dispatchers.Main){
                    AgrolyticsApp.databaseChanged.postValue(Unit)
                    activity.mContentView.removeView(childView)
                    parentDialog.dismiss()
                }
            }
        }
        activity.findViewById<Button>(R.id.cancel_delete_btn).setOnClickListener {
            activity.mContentView.removeView(childView)
            parentDialog.show()
        }
        parentDialog.hide()
    }

    fun startObserverForProcessedImageItem(imageItem : BaseImageItem, index : Int) {
        val workLiveData = workManager.getWorkInfosByTagLiveData("UPLOAD" + imageItem.timestamp.toString())
        workLiveData.removeObservers(activity)
        workLiveData.observe(activity, Observer { listOfWorkInfo ->
            if (listOfWorkInfo.isNullOrEmpty()) {
                itemStateList[index] = ConfigInfo.IMAGE_ITEM_STATE.READY_TO_UPLOAD
                notifyItemChanged(index)
                return@Observer
            }

            val workInfo = listOfWorkInfo[0]    //Csak egy worker létezik a timestamp-pel
            if (workInfo.state.isFinished) {
                workLiveData.removeObservers(activity)
                itemStateList[index] = ConfigInfo.IMAGE_ITEM_STATE.UPLOADED
                doAsync {
                    itemList[index] =
                        dataClient.local.cache.getByTimestamp(imageItem.timestamp)!!
                }
            } else {
                itemStateList[index] = ConfigInfo.IMAGE_ITEM_STATE.BEING_UPLOADED
            }
            notifyItemChanged(index)
        })
    }

    private fun startObserverForDeletedImageItem(imageItem : BaseImageItem, index : Int) {
        val workLiveData = workManager.getWorkInfosByTagLiveData("DELETE" + imageItem.timestamp.toString())
        workLiveData.observe(activity, Observer { listOfWorkInfo ->
            if (listOfWorkInfo.isNullOrEmpty()) {
                return@Observer
            }

            val workInfo = listOfWorkInfo[0]
            if (workInfo.state.isFinished) {
                //Végzett a törlés, a viewModel észreveszi és frissít
                workLiveData.removeObservers(activity)
            } else {
                itemStateList[index] = ConfigInfo.IMAGE_ITEM_STATE.BEING_DELETED
                notifyItemChanged(index)
            }
        })
    }

    //TODO: Egyesítés a RodSelectorPresenter-ben lévő implementációval
    private fun processUnprocessedImageItem(unprocessedImageItem: UnprocessedImageItem) {
        activity.showLoading()
        activity.lifecycleScope.launch(Dispatchers.Main) {
            try {
                val response = MeasurementManager.startOnlineMeasurement(unprocessedImageItem.image!!).awaitResponse()
                if (response.isSuccessful) {
                    val maskImg = response.body()!!.toBitmap()
                    val (maskedImg, numOfWoodPixels) = ImageUtils.drawMaskOnInputImage(unprocessedImageItem.image!!, maskImg)
                    val processedImageItem = ProcessedImageItem(unprocessedImageItem, numOfWoodPixels, maskedImg)
                    activity.hideLoading()
                    MeasurementManager.startApproveMeasurementActivity(activity, processedImageItem, unprocessedImageItem,"online")
                } else {
                    //TODO: Normális exception kezelés
                    activity.hideLoading()
                    activity.toast(activity.getString(R.string.error_retry))
                }
            }
            catch (e : Exception){
                try {
                    Log.d("RecyclerView", "Exception during API call: $e")
                    Log.d("RecyclerView", "Error message: ${e.message}")
                } catch (e: java.lang.Exception) { }
                //TODO: Normális exception kezelés
                activity.hideLoading()
                activity.toast(activity.getString(R.string.error_retry))
            }
        }
    }
}