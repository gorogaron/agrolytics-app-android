package com.agrolytics.agrolytics_android.networking.model

import com.google.gson.annotations.SerializedName

class ImageUploadRequest {
    @SerializedName("image")
    var image: String? = null
}