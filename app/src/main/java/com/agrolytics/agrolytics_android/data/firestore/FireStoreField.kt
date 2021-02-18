package com.agrolytics.agrolytics_android.data.firestore


enum class FireStoreForestryField(var tag: String) {
    NAME("name")
}

enum class FireStoreRoleField(var tag: String) {
    ROLE("name")
}

enum class FireStoreImagesField(var tag: String) {
    FORESTRY_ID("forestryID"),
    LEADER_ID("leaderID"),
    USER_ID("userID"),
    USER_ROLE("role"),
    IMAGE_URL("url"),
    IMAGE_REFERENCE("imageRef"),
    IMAGE_THUMBNAIL_REFERENCE("thumbnailRef"),
    IMAGE_THUMBNAIL_URL("thumbnailUrl"),
    WOOD_TYPE("wood_type"),
    WOOD_LENGTH("length"),
    WOOD_VOLUME("volume"),
    LOC_LAT("lat"),
    LOC_LON("long"),
    TIME("time"),
}

enum class FireStoreUserField(var tag: String) {
    EMAIL("email"),
    FIRST_LOGIN("first_login"),
    FORESTRY_ID("forestry"),
    ROLE("role"),
    LEADER_ID("leaderID")
}