package com.agrolytics.agrolytics_android.ui.measurement.utils

import android.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.Button
import android.widget.ImageView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Delete
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
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
import com.agrolytics.agrolytics_android.utils.SessionManager
import com.agrolytics.agrolytics_android.utils.Util.Companion.getFormattedDateTime
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
                holder.volumeTextView.text = processedImageItem.woodVolume.toString()
            }
            ConfigInfo.IMAGE_ITEM_TYPE.CACHED -> {
                val cachedImageItem = imageItem as CachedImageItem
                holder.volumeTextView.text = cachedImageItem.woodVolume.toString()
            }
            ConfigInfo.IMAGE_ITEM_TYPE.UNPROCESSED -> {
                holder.volumeTextView.text = "Mérésre vár"
            }
        }

        holder.stateTextView.text = when(itemStateList[position]) {
            ConfigInfo.IMAGE_ITEM_STATE.BEING_UPLOADED -> {"Feltöltés alatt"}
            ConfigInfo.IMAGE_ITEM_STATE.READY_TO_UPLOAD -> {"Feltöltésre kész"}
            ConfigInfo.IMAGE_ITEM_STATE.UPLOADED -> {"Feltöltve"}
            ConfigInfo.IMAGE_ITEM_STATE.WAITING_FOR_PROCESSING -> {"Feldolgozásra vár"}
            ConfigInfo.IMAGE_ITEM_STATE.UNDEFINED -> {"Állapot lekérdezés alatt..."}
        }
    }

    fun showImageItemDetails(imageItem: BaseImageItem, index: Int) {
        val builder = AlertDialog.Builder(activity)
        builder.setCancelable(true)
        val view = LayoutInflater.from(activity).inflate(R.layout.dialog_image_item, null, false)
        builder.setView(view)
        val dialog = builder.create()

        view.findViewById<PhotoView>(R.id.image).setImageBitmap(imageItem.image)
        view.findViewById<ImageView>(R.id.btn_delete).setOnClickListener{
            if (itemStateList[index] == ConfigInfo.IMAGE_ITEM_STATE.BEING_UPLOADED ||
                itemStateList[index] == ConfigInfo.IMAGE_ITEM_STATE.UNDEFINED) {
                activity.toast("Feltöltés alatt lévő kép nem törölhető.")
            }
            else {
                showDeleteConfirmation(dialog, imageItem)
            }
        }
        dialog.window!!.setBackgroundDrawableResource(R.drawable.bg_white_round)
        dialog.show()
    }

    private fun showDeleteConfirmation(parentDialog: AlertDialog, imageItem : BaseImageItem) {
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
                            .setInputData(inputData)
                            .build()
                        workManager.enqueue(uploadRequest)
                    }
                    ConfigInfo.IMAGE_ITEM_TYPE.PROCESSED -> {
                        dataClient.local.processed.delete(imageItem as ProcessedImageItem)
                    }
                    ConfigInfo.IMAGE_ITEM_TYPE.UNPROCESSED -> {
                        dataClient.local.unprocessed.delete(imageItem as UnprocessedImageItem)
                    }
                }
                withContext(Dispatchers.Main){
                    itemList.remove(imageItem)
                    notifyDataSetChanged()
                    activity.toast("Kép törölve")
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
        workManager.getWorkInfosByTagLiveData(imageItem.timestamp.toString()).removeObservers(activity)
        workManager.getWorkInfosByTagLiveData(imageItem.timestamp.toString()).observe(activity, Observer { listOfWorkInfo ->
            if (listOfWorkInfo.isNullOrEmpty()) {
                itemStateList[index] = ConfigInfo.IMAGE_ITEM_STATE.READY_TO_UPLOAD
                notifyItemChanged(index)
                return@Observer
            }

            val workInfo = listOfWorkInfo[0]    //Csak egy worker létezik a timestamp-pel
            if (workInfo.state.isFinished) {
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
}