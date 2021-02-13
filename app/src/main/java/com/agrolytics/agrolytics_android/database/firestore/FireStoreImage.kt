package com.agrolytics.agrolytics_android.database.firestore

data class FireStoreItem(
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
)
