package com.agrolytics.agrolytics_android.ui.measurement.utils

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

class SessionRecyclerViewAdapter(var activity : BaseActivity, var itemList : ArrayList<BaseImageItem>) : RecyclerView.Adapter<SessionRecyclerViewAdapter.SessionViewHolder>() {

    inner class SessionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener, KoinComponent {
        private val dataClient: DataClient by inject()
        var imageView = itemView.image
        var volumeTextView = itemView.volume_text
        var dateTextView = itemView.date_text

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val builder = AlertDialog.Builder(activity)
            builder.setCancelable(true)
            val view = LayoutInflater.from(activity).inflate(R.layout.dialog_image_item, null, false)
            builder.setView(view)
            val dialog = builder.create()

            val clickedImageItem = itemList[bindingAdapterPosition]
            view.findViewById<PhotoView>(R.id.image).apply {
                setImageBitmap(when (clickedImageItem.getItemType()) {
                    ConfigInfo.IMAGE_ITEM_TYPE.CACHED -> (clickedImageItem as CachedImageItem).image
                    ConfigInfo.IMAGE_ITEM_TYPE.PROCESSED -> (clickedImageItem as ProcessedImageItem).image
                    ConfigInfo.IMAGE_ITEM_TYPE.UNPROCESSED -> (clickedImageItem as UnprocessedImageItem).image
                })
            }
            view.findViewById<ImageView>(R.id.btn_delete).setOnClickListener {
                val childView = activity.layoutInflater.inflate(R.layout.confirm_delete, null)
                activity.mContentView.addView(childView)
                activity.findViewById<Button>(R.id.confirm_delete_btn).setOnClickListener {
                    GlobalScope.launch(Dispatchers.IO) {
                        when (clickedImageItem.getItemType()) {
                            ConfigInfo.IMAGE_ITEM_TYPE.CACHED -> {
                                dataClient.local.cache.delete(clickedImageItem as CachedImageItem)
                                dataClient.fireBase.fireStore.deleteImage(clickedImageItem.firestoreId)
                                dataClient.fireBase.storage.deleteImage(clickedImageItem.imageUrl)
                            }
                            ConfigInfo.IMAGE_ITEM_TYPE.PROCESSED -> {
                                dataClient.local.processed.delete(clickedImageItem as ProcessedImageItem)
                            }
                            ConfigInfo.IMAGE_ITEM_TYPE.UNPROCESSED -> {
                                dataClient.local.unprocessed.delete(clickedImageItem as UnprocessedImageItem)
                            }
                        }
                        withContext(Dispatchers.Main){
                            activity.toast("Kép törölve")
                            activity.mContentView.removeView(childView)
                            dialog.dismiss()
                        }
                    }
                }
                activity.findViewById<Button>(R.id.cancel_delete_btn).setOnClickListener {
                    activity.mContentView.removeView(childView)
                    dialog.show()
                }
                dialog.hide()
            }
            dialog.window!!.setBackgroundDrawableResource(R.drawable.bg_white_round)
            dialog.show()
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
}