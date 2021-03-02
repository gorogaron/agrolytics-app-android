package com.agrolytics.agrolytics_android.ui.map.utils

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.agrolytics.agrolytics_android.data.local.tables.CachedImageItem

class MarkerInfoBottomSheetDialog : MainBottomSheetDialog() {

    private lateinit var viewOfLayout: View
    private lateinit var mData: CachedImageItem

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        viewOfLayout = inflater.inflate(
//            R.layout.item_images, container,
//            false
//        )
        return viewOfLayout
    }

    fun initData(other: CachedImageItem) {
        mData = other
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView(mData)
    }

    private fun initView(data: CachedImageItem) {
//	    data.imageUrl?.let {
//		    if (it.isNotEmpty()) {
//			    Glide.with(viewOfLayout).load(it).into(viewOfLayout.iv_image)
//		    } else {
//                val bitmap = BitmapFactory.decodeFile(data.localPath)
//                viewOfLayout.iv_image.setImageBitmap(bitmap)
//		    }
//	    }
//
//        if (data.woodLength != null && data.woodVolume != null) {
//            viewOfLayout.tv_volume_adapter.text = context?.getString(R.string.wood_volume_value, (data.woodVolume!! * data.woodLength!!))
//        }
//
//        viewOfLayout.tv_time_image_created_on.text = data.timestamp.toString()
//        viewOfLayout.tv_length_new.text = data.woodLength.toString()
//        viewOfLayout.tv_adapter_id.text = data.id.toString()
    }


    companion object {
        fun instance(): MarkerInfoBottomSheetDialog {
            return MarkerInfoBottomSheetDialog()
        }
    }
}