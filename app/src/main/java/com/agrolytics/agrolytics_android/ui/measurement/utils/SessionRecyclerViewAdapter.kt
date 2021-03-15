package com.agrolytics.agrolytics_android.ui.measurement.utils

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.Button
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.agrolytics.agrolytics_android.R
import com.agrolytics.agrolytics_android.data.DataClient
import com.agrolytics.agrolytics_android.data.local.tables.BaseImageItem
import com.agrolytics.agrolytics_android.data.local.tables.CachedImageItem
import com.agrolytics.agrolytics_android.data.local.tables.ProcessedImageItem
import com.agrolytics.agrolytics_android.data.local.tables.UnprocessedImageItem
import com.agrolytics.agrolytics_android.types.ConfigInfo
import com.agrolytics.agrolytics_android.ui.base.BaseActivity
import com.agrolytics.agrolytics_android.utils.Util.Companion.getFormattedDateTime
import com.github.chrisbanes.photoview.PhotoView
import kotlinx.android.synthetic.main.recycler_view_measurement_item.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.toast
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.collections.ArrayList

class SessionRecyclerViewAdapter(var activity : BaseActivity, var itemList : ArrayList<BaseImageItem>) : RecyclerView.Adapter<SessionRecyclerViewAdapter.SessionViewHolder>(), KoinComponent {

    private val dataClient: DataClient by inject()

    inner class SessionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        var imageView = itemView.image
        var volumeTextView = itemView.volume_text
        var dateTextView = itemView.date_text

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val clickedImageItem = itemList[bindingAdapterPosition]
            showImageItemDetails(clickedImageItem)
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
        when (imageItem.getItemType()) {
            ConfigInfo.IMAGE_ITEM_TYPE.PROCESSED -> {
                val processedImageItem = imageItem as ProcessedImageItem
                holder.imageView.setImageBitmap(processedImageItem.image)
                holder.volumeTextView.text = processedImageItem.woodVolume.toString()
                holder.dateTextView.text = getFormattedDateTime(processedImageItem.timestamp)
            }

            ConfigInfo.IMAGE_ITEM_TYPE.CACHED -> {
                val cachedImageItem = imageItem as CachedImageItem
                holder.imageView.setImageBitmap(cachedImageItem.image)
                holder.volumeTextView.text = cachedImageItem.woodVolume.toString()
                holder.dateTextView.text = getFormattedDateTime(cachedImageItem.timestamp)
            }

            ConfigInfo.IMAGE_ITEM_TYPE.UNPROCESSED -> {
                val unprocessedImageItem = imageItem as UnprocessedImageItem
                holder.imageView.setImageBitmap(unprocessedImageItem.image)
                holder.volumeTextView.text = "Mérésre vár"
                holder.dateTextView.text = getFormattedDateTime(unprocessedImageItem.timestamp)
            }
        }
    }

    fun showImageItemDetails(imageItem: BaseImageItem) {
        val builder = AlertDialog.Builder(activity)
        builder.setCancelable(true)
        val view = LayoutInflater.from(activity).inflate(R.layout.dialog_image_item, null, false)
        builder.setView(view)
        val dialog = builder.create()

        view.findViewById<PhotoView>(R.id.image).setImageBitmap(imageItem.image)
        view.findViewById<ImageView>(R.id.btn_delete).setOnClickListener{showDeleteConfirmation(dialog, imageItem)}
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
                        dataClient.local.cache.delete(imageItem as CachedImageItem)
                        dataClient.fireBase.fireStore.deleteImage(imageItem.firestoreId)
                        dataClient.fireBase.storage.deleteImage(imageItem.imageUrl)
                    }
                    ConfigInfo.IMAGE_ITEM_TYPE.PROCESSED -> {
                        dataClient.local.processed.delete(imageItem as ProcessedImageItem)
                    }
                    ConfigInfo.IMAGE_ITEM_TYPE.UNPROCESSED -> {
                        dataClient.local.unprocessed.delete(imageItem as UnprocessedImageItem)
                    }
                }
                withContext(Dispatchers.Main){
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
}