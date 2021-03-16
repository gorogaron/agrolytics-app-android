package com.agrolytics.agrolytics_android.data.local.tables

import android.graphics.Bitmap
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.agrolytics.agrolytics_android.types.ConfigInfo
import com.agrolytics.agrolytics_android.utils.MeasurementUtils
import com.agrolytics.agrolytics_android.utils.Util.Companion.round
import com.google.firebase.firestore.GeoPoint

@Entity(tableName = "processed_images")
data class ProcessedImageItem(
    @PrimaryKey(autoGenerate = false) override var timestamp: Long,
    @ColumnInfo(name = "session_id") override var sessionId: Long,
    @ColumnInfo(name = "image") override var image: Bitmap?,
    @ColumnInfo(name = "wood_type") override var woodType: String,
    @ColumnInfo(name = "wood_length") override var woodLength: Double,
    @ColumnInfo(name = "location") override var location : GeoPoint,
    @ColumnInfo(name = "wood_volume") var woodVolume: Double
) : BaseImageItem {

    constructor(unprocessedImageItem : UnprocessedImageItem, numOfWoodPixels : Int, maskedImage : Bitmap): this(
        //Primary konstruktor hívása dummy paraméterekkel. TODO: jobb módszert kellene erre keresni
        0, 0, null, "",
        0.0, GeoPoint(0.0,0.0), 0.0)
    {
        val volume = MeasurementUtils.calculateWoodVolume(
            numOfWoodPixels,
            unprocessedImageItem.rodLength,
            unprocessedImageItem.rodLengthPixel,
            unprocessedImageItem.woodLength
        ).round(2)
        this.sessionId = unprocessedImageItem.sessionId
        this.image = maskedImage
        this.woodType = unprocessedImageItem.woodType
        this.woodLength = unprocessedImageItem.woodLength
        this.woodVolume = volume
        this.location = unprocessedImageItem.location
        this.timestamp = unprocessedImageItem.timestamp
    }

    override fun getItemType(): ConfigInfo.IMAGE_ITEM_TYPE {
        return ConfigInfo.IMAGE_ITEM_TYPE.PROCESSED
    }
}