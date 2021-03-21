package com.agrolytics.agrolytics_android.ui.images.recyclerview

import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.agrolytics.agrolytics_android.R
import com.agrolytics.agrolytics_android.ui.base.BaseActivity
import com.agrolytics.agrolytics_android.ui.measurement.MeasurementManager
import com.agrolytics.agrolytics_android.utils.Util.Companion.getFormattedDateTime
import kotlinx.android.synthetic.main.recycler_view_session_item.view.*
import kotlin.collections.ArrayList

class ImagesRecyclerViewAdapter(var activity : BaseActivity, var itemList : ArrayList<SessionItem>) : RecyclerView.Adapter<ImagesRecyclerViewAdapter.SessionViewHolder>() {

    inner class SessionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        var lengthTextView = itemView.length_value
        var typeTextView = itemView.wood_type_value
        var volumeTextView = itemView.wood_volume_value
        var dateTextView = itemView.date_value
        var sessionImage = itemView.image

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val sessionIdToShow = itemList[bindingAdapterPosition].sessionId
            MeasurementManager.showSession(activity, sessionIdToShow)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_session_item, parent, false)
        return SessionViewHolder(view)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
        val sessionItem = itemList[position]
        holder.lengthTextView.text = if (sessionItem.woodLength >= 0) sessionItem.woodLength.toString() else "Változó"
        holder.typeTextView.text = if (sessionItem.woodType != "") sessionItem.woodType else "Változó"
        holder.volumeTextView.text = if (sessionItem.woodVolume >= 0) sessionItem.woodVolume.toString() else "Nincs kész"
        holder.dateTextView.text = getFormattedDateTime(sessionItem.sessionId)
        holder.sessionImage.setImageBitmap(sessionItem.sessionImage)
    }
}