package com.agrolytics.agrolytics_android.database.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class FireStoreDB {
    val db = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance("gs://webhost-7bf8f.appspot.com")
}