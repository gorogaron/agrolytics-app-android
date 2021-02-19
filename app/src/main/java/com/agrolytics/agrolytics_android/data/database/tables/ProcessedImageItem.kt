package com.agrolytics.agrolytics_android.data.database.tables

import android.graphics.Bitmap
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.GeoPoint
import java.io.Serializable
import java.sql.Timestamp

@Entity(tableName = "processed_images")
data class ProcessedImageItem(
    @PrimaryKey(autoGenerate = true) var id: Long,
    @ColumnInfo(name = "session_id") var sessionId: String,
    @ColumnInfo(name = "image") var image: Bitmap,
    @ColumnInfo(name = "wood_type") var woodType: String,
    @ColumnInfo(name = "wood_length") var woodLength: Double,
    @ColumnInfo(name = "wood_volume") var woodVolume: Double,
    @ColumnInfo(name = "lat") var lat: Double,
    @ColumnInfo(name = "lon") var lon: Double,
    @ColumnInfo(name = "timestamp") var timestamp: Long
) : Serializable