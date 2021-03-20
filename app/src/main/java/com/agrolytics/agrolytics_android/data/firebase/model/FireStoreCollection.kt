package com.agrolytics.agrolytics_android.data.firebase.model

enum class FireStoreCollection(var tag: String) {
    USER("user"),
    FORESTRY("forestry"),
    IMAGES("images"),
    DELETED_IMAGES("deleted_images"),
    ROLE("role")
}