package com.agrolytics.agrolytics_android.data.local.tables

import android.media.Image
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.agrolytics.agrolytics_android.types.ConfigInfo
import com.google.firebase.firestore.GeoPoint

@Entity(tableName = "cached_images")
data class CachedImageItem (
    @PrimaryKey(autoGenerate = true) var id: Long,
    @ColumnInfo(name = "session_id") var sessionId: String,
    @ColumnInfo(name = "forestry_id") var forestryId: String,
    @ColumnInfo(name = "leader_id") var leaderId: String?,
    @ColumnInfo(name = "user_id") var userId: String,
    @ColumnInfo(name = "user_role") var userRole: String,
    @ColumnInfo(name = "image_ref") var imageRef: String,
    @ColumnInfo(name = "image_url") var imageUrl: String,
    @ColumnInfo(name = "thumb_ref") var thumbRef: String,
    @ColumnInfo(name = "thumb_url") var thumbUrl: String,
    @ColumnInfo(name = "wood_type") var woodType: String,
    @ColumnInfo(name = "wood_length") var woodLength: Double,
    @ColumnInfo(name = "wood_volume") var woodVolume: Double,
    @ColumnInfo(name = "location") var location: GeoPoint,
    @ColumnInfo(name = "timestamp") var timestamp: Long,
    @ColumnInfo(name = "firestore_id") var firestoreId: String,
    @ColumnInfo(name = "local_path") var localPath: String
) : ImageItemBase {

    override fun getItemType(): ConfigInfo.IMAGE_ITEM_TYPE {
        return ConfigInfo.IMAGE_ITEM_TYPE.CACHED
    }
}