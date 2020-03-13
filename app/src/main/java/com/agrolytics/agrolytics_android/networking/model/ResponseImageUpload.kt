package com.agrolytics.agrolytics_android.networking.model

import android.os.Parcel
import android.os.Parcelable

class ResponseImageUpload() : Parcelable {
    var result: String? = null
    var image: String? = null

    constructor(parcel: Parcel) : this() {
        result = parcel.readString()
        image = parcel.readString()
    }

    constructor(b64: String, volume: String) : this() {
        result = volume
        image = b64
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(result)
        parcel.writeString(image)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ResponseImageUpload> {
        override fun createFromParcel(parcel: Parcel): ResponseImageUpload {
            return ResponseImageUpload(parcel)
        }

        override fun newArray(size: Int): Array<ResponseImageUpload?> {
            return arrayOfNulls(size)
        }
    }
}