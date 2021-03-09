package com.agrolytics.agrolytics_android.data.local.tables

import android.graphics.Bitmap
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.agrolytics.agrolytics_android.types.ConfigInfo
import com.google.firebase.firestore.GeoPoint

@Entity(tableName = "unprocessed_images")
data class UnprocessedImageItem(
    @PrimaryKey(autoGenerate = false) var timestamp: Long,
    @ColumnInfo(name = "session_id") var sessionId: Long,
    @ColumnInfo(name = "image") var image: Bitmap,
    @ColumnInfo(name = "wood_type") var woodType: String,
    @ColumnInfo(name = "wood_length") var woodLength: Double,
    @ColumnInfo(name = "location") var location: GeoPoint,
    @ColumnInfo(name = "rod_length") var rodLength: Double,
    @ColumnInfo(name = "rod_length_pixel") var rodLengthPixel: Double
) : BaseImageItem
{

    fun toProcessedImageItem(maskedImage : Bitmap, volume : Double) : ProcessedImageItem{
        return ProcessedImageItem(
            sessionId = sessionId,
            image = maskedImage,
            woodType = woodType,
            woodLength = woodLength,
            woodVolume = volume,
            location = location,
            timestamp = timestamp
        )
    }

    override fun getItemType(): ConfigInfo.IMAGE_ITEM_TYPE {
        return ConfigInfo.IMAGE_ITEM_TYPE.UNPROCESSED
    }

}
