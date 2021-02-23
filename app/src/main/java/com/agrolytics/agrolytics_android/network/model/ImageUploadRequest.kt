package com.agrolytics.agrolytics_android.network.model

import com.google.gson.annotations.SerializedName

class ImageUploadRequest {
    @SerializedName("image")
    var image: String? = null
}