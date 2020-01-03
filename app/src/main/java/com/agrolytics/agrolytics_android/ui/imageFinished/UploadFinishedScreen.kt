package com.agrolytics.agrolytics_android.ui.imageFinished

import com.agrolytics.agrolytics_android.base.BaseScreen
import com.agrolytics.agrolytics_android.networking.model.ResponseImageUpload
import com.agrolytics.agrolytics_android.ui.imageFinished.fragment.UploadFinishedFragment

interface UploadFinishedScreen: BaseScreen {
    fun updateView(fragment: UploadFinishedFragment)
    fun onDeclineClicked(fragment: UploadFinishedFragment, id: String?)
    fun onAcceptClicked(responseImageUpload: ResponseImageUpload?, path: String?, fragment: UploadFinishedFragment, id: String?)
    fun onNextPage(fragment: UploadFinishedFragment)
    fun onPreviousPage(fragment: UploadFinishedFragment)
}