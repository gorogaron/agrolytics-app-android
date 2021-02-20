package com.agrolytics.agrolytics_android.ui.measurement.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.agrolytics.agrolytics_android.R
import com.agrolytics.agrolytics_android.data.local.tables.ImageItemBase
import com.agrolytics.agrolytics_android.types.ConfigInfo
import kotlinx.android.synthetic.main.recycler_view_measurement_item.view.*

class SessionRecyclerViewAdapter(var itemList : ArrayList<ImageItemBase>) : RecyclerView.Adapter<SessionRecyclerViewAdapter.SessionViewHolder>() {

    inner class SessionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageView = itemView.image
        var volumeTextView = itemView.volume_text
        var dateTextView = itemView.date_text
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_images, parent, false)
        return SessionViewHolder(view)
    }

    override fun getItemCount(): Int {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
        val imageItem = itemList[position]
        if (imageItem.getItemType() == ConfigInfo.IMAGE_ITEM_TYPE.PROCESSED) {
            // TODO: Bind processed image item
        }
        else if (imageItem.getItemType() == ConfigInfo.IMAGE_ITEM_TYPE.UNPROCESSED) {
            // TODO: Bind unprocessed image item
        }
    }
}