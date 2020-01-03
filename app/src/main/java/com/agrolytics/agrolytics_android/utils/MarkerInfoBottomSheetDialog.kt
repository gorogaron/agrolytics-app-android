package com.agrolytics.agrolytics_android.utils

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.agrolytics.agrolytics_android.R
import com.agrolytics.agrolytics_android.networking.model.ImageItem
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_images.view.*

class MarkerInfoBottomSheetDialog : MainBottomSheetDialog() {

    private lateinit var viewOfLayout: View
    private lateinit var mData: ImageItem

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewOfLayout = inflater.inflate(
            R.layout.item_images, container,
            false
        )
        return viewOfLayout
    }

    fun initData(other: ImageItem) {
        mData = other
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView(mData)
    }

    private fun initView(data: ImageItem) {
	    data.serverImage?.let {
		    if (it.isNotEmpty()) {
			    Glide.with(viewOfLayout).load(it).into(viewOfLayout.iv_image)
		    } else {
                val bitmap = BitmapFactory.decodeFile(data.localPath)
                viewOfLayout.iv_image.setImageBitmap(bitmap)
		    }
	    }

        if (data.length != null && data.volume != null) {
            viewOfLayout.tv_volume_adapter.text = context?.getString(R.string.wood_volume_value, (data.volume!! * data.length!!))
        }

        viewOfLayout.tv_time_image_created_on.text = data.time
        viewOfLayout.tv_length_new.text = data.length.toString()
        viewOfLayout.tv_adapter_id.text = data.id
    }


    companion object {
        fun instance(): MarkerInfoBottomSheetDialog {
            return MarkerInfoBottomSheetDialog()
        }
    }
}