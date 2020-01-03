package com.agrolytics.agrolytics_android.networking.model

import com.google.gson.annotations.SerializedName

class ImageUploadRequest {

    @SerializedName("process_type")
    var processType: String? = null

    @SerializedName("rod_length")
    var rodLength: Double? = null

    @SerializedName("rod_length_pixel")
    var rodLengthPixel: Int? = null

    @SerializedName("image")
    var image: String? = null
}