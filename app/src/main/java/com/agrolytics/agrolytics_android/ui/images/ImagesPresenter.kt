package com.agrolytics.agrolytics_android.ui.images

import android.content.Context
import com.agrolytics.agrolytics_android.ui.base.BasePresenter

class ImagesPresenter(val context: Context) : BasePresenter<ImagesScreen>() {

    private var activity: ImagesActivity? = null

    fun setActivity(activity: ImagesActivity) {
        this.activity = activity
    }
}