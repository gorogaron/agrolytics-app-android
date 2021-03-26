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
                    ConfigInfo.IMAGE_ITEM_TYPE.CACHED -> itemStateList.add(ConfigInfo.IMAGE_ITEM_STATE.UPLOADED)
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
                holder.volumeTextView.text = processedImageItem.woodVolume.toString()
            }
            ConfigInfo.IMAGE_ITEM_TYPE.CACHED -> {
                val cachedImageItem = imageItem as CachedImageItem
                holder.volumeTextView.visibility = View.VISIBLE
                holder.uploadButton.visibility = View.GONE
                holder.volumeTextView.text = cachedImageItem.woodVolume.toString()
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
            ConfigInfo.IMAGE_ITEM_STATE.BEING_UPLOADED -> {"Feltöltés alatt"}
            ConfigInfo.IMAGE_ITEM_STATE.READY_TO_UPLOAD -> {"Feltöltésre kész"}
            ConfigInfo.IMAGE_ITEM_STATE.UPLOADED -> {"Feltöltve"}
            ConfigInfo.IMAGE_ITEM_STATE.WAITING_FOR_PROCESSING -> {"Feldolgozásra vár"}
            ConfigInfo.IMAGE_ITEM_STATE.UNDEFINED -> {""}
            ConfigInfo.IMAGE_ITEM_STATE.BEING_DELETED -> {"Törlés alatt"}
        }
    }

    fun showImageItemDetails(imageItem: BaseImageItem, index: Int) {
        val builder = AlertDialog.Builder(activity)
        builder.setCancelable(true)
        val view = LayoutInflater.from(activity).inflate(R.layout.dialog_image_item, null, false)
        builder.setView(view)
        val dialog = builder.create()

        view.findViewById<TextView>(R.id.volume).text = when (imageItem.getItemType()) {
            ConfigInfo.IMAGE_ITEM_TYPE.CACHED -> (imageItem as CachedImageItem).woodVolume.round(2).toString()
            ConfigInfo.IMAGE_ITEM_TYPE.PROCESSED -> (imageItem as ProcessedImageItem).woodVolume.round(2).toString()
            ConfigInfo.IMAGE_ITEM_TYPE.UNPROCESSED -> "Mérésre vár"
        }
        view.findViewById<TextView>(R.id.length).text = imageItem.woodLength.toString()
        view.findViewById<TextView>(R.id.species).text = imageItem.woodType
        view.findViewById<TextView>(R.id.date).text = getFormattedDateTime(imageItem.timestamp)
        view.findViewById<PhotoView>(R.id.image).setImageBitmap(imageItem.image)
        view.findViewById<ImageView>(R.id.btn_delete).setOnClickListener{
            if (itemStateList[index] == ConfigInfo.IMAGE_ITEM_STATE.BEING_UPLOADED ||
                itemStateList[index] == ConfigInfo.IMAGE_ITEM_STATE.UNDEFINED) {
                activity.toast("Feltöltés alatt lévő kép nem törölhető.")
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
                        withContext(Dispatchers.Main) {
                            startObserverForDeletedImageItem(imageItem, index)
                        }
                    }
                    ConfigInfo.IMAGE_ITEM_TYPE.PROCESSED -> {
                        dataClient.local.processed.delete(imageItem as ProcessedImageItem)
                    }
                    ConfigInfo.IMAGE_ITEM_TYPE.UNPROCESSED -> {
                        dataClient.local.unprocessed.delete(imageItem as UnprocessedImageItem)
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
                val response = MeasurementManager.startOnlineMeasurement(unprocessedImageItem.image!!)
                if (response.isSuccessful) {
                    val maskImg = response.body()!!.toBitmap()
                    val (maskedImg, numOfWoodPixels) = ImageUtils.drawMaskOnInputImage(unprocessedImageItem.image!!, maskImg)
                    val processedImageItem = ProcessedImageItem(unprocessedImageItem, numOfWoodPixels, maskedImg)
                    activity.hideLoading()
                    MeasurementManager.startApproveMeasurementActivity(activity, processedImageItem, unprocessedImageItem,"online")
                } else {
                    //TODO: Normális exception kezelés
                    activity.hideLoading()
                    activity.toast("Hiba történt, próbáld újra.")
                }
            }
            catch (e : Exception){
                //TODO: Normális exception kezelés
                activity.hideLoading()
                activity.toast("Hiba történt, próbáld újra.")
            }
        }
    }
}