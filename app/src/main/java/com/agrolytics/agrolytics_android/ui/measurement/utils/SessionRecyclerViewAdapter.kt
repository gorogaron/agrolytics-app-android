package com.agrolytics.agrolytics_android.ui.measurement.utils

import android.app.Activity
import android.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.agrolytics.agrolytics_android.R
import com.agrolytics.agrolytics_android.data.local.tables.BaseImageItem
import com.agrolytics.agrolytics_android.data.local.tables.CachedImageItem
import com.agrolytics.agrolytics_android.data.local.tables.ProcessedImageItem
import com.agrolytics.agrolytics_android.data.local.tables.UnprocessedImageItem
import com.agrolytics.agrolytics_android.types.ConfigInfo
import com.agrolytics.agrolytics_android.ui.measurement.activity.SessionActivity
import com.agrolytics.agrolytics_android.utils.Util.Companion.getFormattedDateTime
import com.github.chrisbanes.photoview.PhotoView
import kotlinx.android.synthetic.main.recycler_view_measurement_item.view.*
import kotlin.collections.ArrayList

class SessionRecyclerViewAdapter(var activity : Activity, var itemList : ArrayList<BaseImageItem>) : RecyclerView.Adapter<SessionRecyclerViewAdapter.SessionViewHolder>() {

    inner class SessionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
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
            view.findViewById<PhotoView>(R.id.image).apply {
                setImageBitmap(when (itemList[bindingAdapterPosition].getItemType()) {
                    ConfigInfo.IMAGE_ITEM_TYPE.CACHED -> ((itemList[bindingAdapterPosition]) as CachedImageItem).image
                    ConfigInfo.IMAGE_ITEM_TYPE.PROCESSED -> ((itemList[bindingAdapterPosition]) as ProcessedImageItem).image
                    ConfigInfo.IMAGE_ITEM_TYPE.UNPROCESSED -> ((itemList[bindingAdapterPosition]) as UnprocessedImageItem).image
                })
            }
            view.findViewById<ImageView>(R.id.btn_delete).setOnClickListener {
                val rootView = activity.window.decorView.rootView as ViewGroup
                val childView = activity.layoutInflater.inflate(R.layout.confirm_delete, null)
                rootView.addView(childView)
            }
            builder.setView(view)
            val dialog = builder.create()
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
        if (imageItem.getItemType() == ConfigInfo.IMAGE_ITEM_TYPE.PROCESSED) {
            val processedImageItem = imageItem as ProcessedImageItem
            holder.imageView.setImageBitmap(processedImageItem.image)
            holder.volumeTextView.text = processedImageItem.woodVolume.toString()
            holder.dateTextView.text = getFormattedDateTime(processedImageItem.timestamp)
        }
        else if (imageItem.getItemType() == ConfigInfo.IMAGE_ITEM_TYPE.UNPROCESSED) {
            val unprocessedImageItem = imageItem as UnprocessedImageItem
            holder.imageView.setImageBitmap(unprocessedImageItem.image)
            holder.volumeTextView.text = "Mérésre vár"
            holder.dateTextView.text = getFormattedDateTime(unprocessedImageItem.timestamp)
        }
    }
}