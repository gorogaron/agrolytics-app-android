package com.agrolytics.agrolytics_android.data.firebase

import com.agrolytics.agrolytics_android.data.firebase.model.FireStoreCollection
import com.agrolytics.agrolytics_android.data.firebase.model.FireStoreImageItem
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class FireStore {
    var firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    suspend fun uploadToFireStore(
        fireStoreImageItem: FireStoreImageItem
    ) : String = suspendCoroutine { cont ->
        firestore?.collection(FireStoreCollection.IMAGES.tag)?.add(fireStoreImageItem.toHashMap())
            ?.addOnSuccessListener {
                cont.resume(it.id)
            }
            ?.addOnFailureListener{
                cont.resumeWithException(it)
            }
    }

    suspend fun getUserDocumentById(userId: String) : DocumentSnapshot = suspendCoroutine { cont->
        val userDocumentReference = firestore.collection(
            FireStoreCollection.USER.tag).document(userId)
        userDocumentReference.get()
            .addOnSuccessListener { cont.resume(it) }
            .addOnFailureListener { cont.resumeWithException(it) }
    }

    suspend fun getForestryDocumentById(forestryId: String) : DocumentSnapshot = suspendCoroutine { cont->
        val userDocumentReference = firestore.collection(
            FireStoreCollection.FORESTRY.tag).document(forestryId)
        userDocumentReference.get()
            .addOnSuccessListener { cont.resume(it) }
            .addOnFailureListener { cont.resumeWithException(it) }
    }

    suspend fun getRoleDocumentByRef(roleRef : DocumentReference) : DocumentSnapshot = suspendCoroutine { cont->
        roleRef.get()
            .addOnSuccessListener { cont.resume(it) }
            .addOnFailureListener { cont.resumeWithException(it) }
    }

}