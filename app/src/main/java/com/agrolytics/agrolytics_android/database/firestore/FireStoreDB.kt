package com.agrolytics.agrolytics_android.database.firestore

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class FireStoreDB {

    private val url: String = "gs://webhost-7bf8f.appspot.com"

    var db: FirebaseFirestore? = null
    var storage: FirebaseStorage? = null

    init {
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance(url)
    }
}