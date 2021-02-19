package com.agrolytics.agrolytics_android.data.database.tables

import android.graphics.Bitmap
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.GeoPoint
import java.sql.Timestamp

@Entity(tableName = "unprocessed_images")
data class UnprocessedImageItem(
    @PrimaryKey(autoGenerate = true) var id: Long,
    @ColumnInfo(name = "session_id") var sessionId: String,
    @ColumnInfo(name = "image") var image: Bitmap,
    @ColumnInfo(name = "wood_type") var woodType: String,
    @ColumnInfo(name = "wood_length") var woodLength: Double,
    @ColumnInfo(name = "lat") var lat: Double,
    @ColumnInfo(name = "lon") var lon: Double,
    @ColumnInfo(name = "rod_length") var rodLength: Double,
    @ColumnInfo(name = "rod_length_pixel") var rodLengthPixel: Double,
    @ColumnInfo(name = "timestamp") var timestamp: Long
)
{
    fun toProcessedImageItem(maskedImage : Bitmap, volume : Double) : ProcessedImageItem{
        return ProcessedImageItem(
            id = id,
            sessionId = sessionId,
            image = maskedImage,
            woodType = woodType,
            woodLength = woodLength,
            woodVolume = volume,
            lat = lat,
            lon = lon,
            timestamp = timestamp
        )
    }

}
