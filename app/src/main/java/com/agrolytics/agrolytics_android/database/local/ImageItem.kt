package com.agrolytics.agrolytics_android.database.local

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "Images")
data class ImageItem(

    @PrimaryKey
    var id: String,

    @ColumnInfo(name = "session_id")
    var session_id: String,

    @ColumnInfo(name = "local_path")
    var localPath: String? = null,

    @ColumnInfo(name = "pushed_to_server")
    var isPushedToServer: Boolean = false,

    @ColumnInfo(name = "lat")
    var latitude: Double? = null,

    @ColumnInfo(name = "lng")
    var longitude: Double? = null,

    @ColumnInfo(name = "length")
    var length: Double? = null,

    @ColumnInfo(name = "volume")
    var volume: Double? = null,

    @ColumnInfo(name = "time")
    var time: String? = null,

    @ColumnInfo(name = "server_image")
    var serverImage: String? = null,

    @ColumnInfo(name = "server_path")
    var serverPath: String? = null,

    @ColumnInfo(name = "user_id")
    var userID: String? = null,

    @ColumnInfo(name = "leader_id")
    var leaderID: String? = null,

    @ColumnInfo(name = "forestry_id")
    var forestryID: String? = null,

    @ColumnInfo(name = "firestore_id")
    var firestoreId: String? = null,

    @ColumnInfo(name = "rod_length")
    var rodLength: Double? = null,

    @ColumnInfo(name = "rod_length_pixel")
    var rodLengthPixel: Int? = null,

    @ColumnInfo(name = "thumbnail_path")
    var thumbnailPath: String? = null,

    @ColumnInfo(name = "thumbnail_url")
    var thumbnailUrl: String? = null,

    @ColumnInfo(name = "wood_type")
    var woodType: String? = null

) : Parcelable {

//    @PrimaryKey(autoGenerate = true)
//    var id: Long? = null

    var isChecked = false

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
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
}