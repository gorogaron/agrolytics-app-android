package com.agrolytics.agrolytics_android.data.firestore

data class FireStoreImageItem(
    val forestryId: String,
    val leaderId: String,
    val userId: String,
    val userRole: String,
    val imageUrl: String,
    val imageRef: String,
    val thumbnailRef: String,
    val thumbnailUrl: String,
    val woodType: String? = null,
    val woodLength: Double,
    val woodVolume: Double,
    val locLat: Double? = null,
    val locLon: Double? = null,
    val timestamp: String
) {
    fun toHashMap() : HashMap<String, Any?> {
        return hashMapOf(
            FireStoreImagesField.FORESTRY_ID.tag to this.forestryId,
            FireStoreImagesField.LEADER_ID.tag to this.userId,
            FireStoreImagesField.TIME.tag to this.timestamp,
            FireStoreImagesField.USER_ID.tag to this.userId,
            FireStoreImagesField.USER_ROLE.tag to this.userRole,
            FireStoreImagesField.IMAGE_URL.tag to this.imageUrl,
            FireStoreImagesField.IMAGE_REFERENCE.tag to this.imageRef,
            FireStoreImagesField.IMAGE_THUMBNAIL_URL.tag to this.thumbnailUrl,
            FireStoreImagesField.IMAGE_THUMBNAIL_REFERENCE.tag to thumbnailRef,
            FireStoreImagesField.WOOD_TYPE.tag to this.woodType,
            FireStoreImagesField.WOOD_LENGTH.tag to this.woodLength,
            FireStoreImagesField.WOOD_VOLUME.tag to this.woodLength,
            FireStoreImagesField.LOC_LAT.tag to this.locLat,
            FireStoreImagesField.LOC_LON.tag to this.locLon
        )
    }
}
