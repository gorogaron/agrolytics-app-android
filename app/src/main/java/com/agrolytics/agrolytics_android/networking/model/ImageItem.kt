package com.agrolytics.agrolytics_android.networking.model

import android.os.Parcel
import android.os.Parcelable

data class ImageItem(

    var id: String = "",

    var localPath: String? = null,

    var isPushedToServer: Boolean = false,

    var latitude: Double? = null,

    var longitude: Double? = null,


    var length: Double? = null,

    var volume: Double? = null,

    var time: String? = null,

    var serverImage: String? = null,

    var serverPath: String? = null,

    var userID: String? = null,

    var leaderID: String? = null,

    var forestryID: String? = null,

    var firestoreId: String? = null,

    var selectionMode: String? = null,

    var rodLength: Double? = null,

    var rodLengthPixel: Int? = null,

    var thumbnailPath: String? = null,

    var thumbnailUrl: String? = null,

    var woodType: String? = null

) : Parcelable {

//    @PrimaryKey(autoGenerate = true)
//    var id: Long? = null

    var isChecked = false

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readByte() != 0.toByte(),
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
        isChecked = parcel.readByte() != 0.toByte()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(localPath)
        parcel.writeByte(if (isPushedToServer) 1 else 0)
        parcel.writeValue(latitude)
        parcel.writeValue(longitude)
        parcel.writeValue(length)
        parcel.writeValue(volume)
        parcel.writeString(time)
        parcel.writeString(serverImage)
        parcel.writeString(serverPath)
        parcel.writeString(userID)
        parcel.writeString(leaderID)
        parcel.writeString(forestryID)
        parcel.writeString(firestoreId)
        parcel.writeString(selectionMode)
        parcel.writeValue(rodLength)
        parcel.writeValue(rodLengthPixel)
        parcel.writeByte(if (isChecked) 1 else 0)
        parcel.writeString(thumbnailPath)
        parcel.writeString(thumbnailUrl)
        parcel.writeString(woodType)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ImageItem> {
        override fun createFromParcel(parcel: Parcel): ImageItem {
            return ImageItem(parcel)
        }

        override fun newArray(size: Int): Array<ImageItem?> {
            return arrayOfNulls(size)
        }
    }

    fun copy(imageItem: ImageItem): ImageItem {
        val newItem = ImageItem(
            id= imageItem.id,
            time= imageItem.time
        )
        return newItem
    }

}