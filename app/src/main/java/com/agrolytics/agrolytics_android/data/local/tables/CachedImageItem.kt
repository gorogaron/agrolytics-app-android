package com.agrolytics.agrolytics_android.data.local.tables

import android.graphics.Bitmap
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.agrolytics.agrolytics_android.data.firebase.model.FireStoreImageItem
import com.agrolytics.agrolytics_android.types.ConfigInfo
import com.google.firebase.firestore.GeoPoint

@Entity(tableName = "cached_images")
data class CachedImageItem (
    @PrimaryKey(autoGenerate = false) override var timestamp: Long,
    @ColumnInfo(name = "session_id") override var sessionId: Long,
    @ColumnInfo(name = "image") override var image: Bitmap?,
    @ColumnInfo(name = "wood_type") override var woodType: String,
    @ColumnInfo(name = "wood_length") override var woodLength: Double,
    @ColumnInfo(name = "location") override var location: GeoPoint,
    @ColumnInfo(name = "forestry_id") var forestryId: String,
    @ColumnInfo(name = "leader_id") var leaderId: String?,
    @ColumnInfo(name = "user_id") var userId: String,
    @ColumnInfo(name = "user_role") var userRole: String,
    @ColumnInfo(name = "wood_volume") var woodVolume: Double,
    @ColumnInfo(name = "firestore_id") var firestoreId: String,
    @ColumnInfo(name = "added_wood_volume") var addedWoodVolume: Double,
    @ColumnInfo(name = "added_wood_volume_justification") var addedWoodVolumeJustification: String
) : BaseImageItem {

    constructor(firestoreItem: FireStoreImageItem) : this(
        timestamp = firestoreItem.timestamp,
        sessionId = firestoreItem.sessionId,
        forestryId = firestoreItem.forestryId,
        leaderId = firestoreItem.leaderId,
        userId = firestoreItem.userId,
        userRole = firestoreItem.userRole,
        woodType = firestoreItem.woodType!!,
        woodLength = firestoreItem.woodLength,
        woodVolume = firestoreItem.woodVolume,
        location = firestoreItem.location!!,
        image = null,
        firestoreId = firestoreItem.firestoreId,
        addedWoodVolume = firestoreItem.addedWoodVolume,
        addedWoodVolumeJustification = firestoreItem.addedWoodVolumeJustification
    )

    override fun getItemType(): ConfigInfo.IMAGE_ITEM_TYPE {
        return ConfigInfo.IMAGE_ITEM_TYPE.CACHED
    }
}