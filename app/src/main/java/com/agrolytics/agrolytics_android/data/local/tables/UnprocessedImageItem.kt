package com.agrolytics.agrolytics_android.data.local.tables

import android.graphics.Bitmap
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.agrolytics.agrolytics_android.types.ConfigInfo
import com.google.firebase.firestore.GeoPoint

@Entity(tableName = "unprocessed_images")
data class UnprocessedImageItem(
    @PrimaryKey(autoGenerate = false) override var timestamp: Long,
    @ColumnInfo(name = "session_id") override var sessionId: Long,
    @ColumnInfo(name = "image") override var image: Bitmap?,
    @ColumnInfo(name = "wood_type") override var woodType: String,
    @ColumnInfo(name = "wood_length") override var woodLength: Double,
    @ColumnInfo(name = "location") override var location: GeoPoint,
    @ColumnInfo(name = "rod_length") var rodLength: Double,
    @ColumnInfo(name = "rod_length_pixel") var rodLengthPixel: Double
) : BaseImageItem
{

    override fun getItemType(): ConfigInfo.IMAGE_ITEM_TYPE {
        return ConfigInfo.IMAGE_ITEM_TYPE.UNPROCESSED
    }

}
