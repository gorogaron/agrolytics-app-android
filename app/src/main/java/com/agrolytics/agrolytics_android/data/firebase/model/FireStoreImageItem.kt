package com.agrolytics.agrolytics_android.data.firebase.model

import com.google.firebase.firestore.GeoPoint

data class FireStoreImageItem(
    val timestamp: Long,
    val sessionId: Long,
    val forestryId: String,
    val leaderId: String,
    val userId: String,
    val userRole: String,
    val woodType: String? = null,
    val woodLength: Double,
    val woodVolume: Double,
    val location: GeoPoint? = null,
    var firestoreId: String
) {
    fun toHashMap() : HashMap<String, Any?> {
        return hashMapOf(
            FireStoreImagesField.TIMESTAMP.tag to this.timestamp,
            FireStoreImagesField.SESSION_ID.tag to this.sessionId,
            FireStoreImagesField.FORESTRY_ID.tag to this.forestryId,
            FireStoreImagesField.LEADER_ID.tag to this.userId,
            FireStoreImagesField.USER_ID.tag to this.userId,
            FireStoreImagesField.USER_ROLE.tag to this.userRole,
            FireStoreImagesField.WOOD_TYPE.tag to this.woodType,
            FireStoreImagesField.WOOD_LENGTH.tag to this.woodLength,
            FireStoreImagesField.WOOD_VOLUME.tag to this.woodVolume,
            FireStoreImagesField.LOCATION.tag to this.location
            )
    }
}
