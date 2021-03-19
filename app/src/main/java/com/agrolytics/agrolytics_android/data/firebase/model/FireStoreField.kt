package com.agrolytics.agrolytics_android.data.firebase.model


enum class FireStoreForestryField(var tag: String) {
    EMAIL("email"),
    NAME("name"),
    EXPIRATION("expiration")
}

enum class FireStoreRoleField(var tag: String) {
    ROLE("name")
}

enum class FireStoreImagesField(var tag: String) {
    TIMESTAMP("timestamp"),
    SESSION_ID("session_id"),
    FORESTRY_ID("forestry_id"),
    LEADER_ID("leader_id"),
    USER_ID("user_id"),
    USER_ROLE("user_role"),
    WOOD_TYPE("wood_type"),
    WOOD_LENGTH("wood_length"),
    WOOD_VOLUME("wood_volume"),
    LOCATION("location")
}

enum class FireStoreUserField(var tag: String) {
    EMAIL("email"),
    FORESTRY_ID("forestry_id"),
    ROLE("role"),
    LEADER_ID("leader_id")
}