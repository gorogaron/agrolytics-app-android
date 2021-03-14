package com.agrolytics.agrolytics_android.data.firebase

import android.util.Log
import com.agrolytics.agrolytics_android.data.firebase.model.FireStoreCollection
import com.agrolytics.agrolytics_android.data.firebase.model.FireStoreImageItem
import com.agrolytics.agrolytics_android.data.firebase.model.FireStoreImagesField
import com.agrolytics.agrolytics_android.utils.SessionManager
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class FireStore: KoinComponent {

    private val sessionManager: SessionManager by inject()

    var firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    suspend fun uploadToFireStore(
        fireStoreImageItem: FireStoreImageItem
    ): String = suspendCoroutine { cont ->
        firestore.collection(FireStoreCollection.IMAGES.tag).add(fireStoreImageItem.toHashMap())
            .addOnSuccessListener {
                cont.resume(it.id)
            }
            .addOnFailureListener {
                cont.resumeWithException(it)
            }
    }

    suspend fun downloadFromFireStore(
        timestamps: List<Long>
    ): List<FireStoreImageItem> {
        val firestoreImageItems = ArrayList<FireStoreImageItem>()
        return suspendCoroutine { cont ->
            if (timestamps.size == 0) {
                timestamps
            }
            firestore.collection(FireStoreCollection.IMAGES.tag)
                .whereEqualTo(FireStoreImagesField.USER_ID.tag, sessionManager.userId)
                .whereNotIn(FireStoreImagesField.TIMESTAMP.tag, timestamps)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val data = document.data
                        val firestoreImageItem = FireStoreImageItem(
                            timestamp = data[FireStoreImagesField.TIMESTAMP.tag] as Long,
                            sessionId = data[FireStoreImagesField.SESSION_ID.tag] as Long,
                            forestryId = data[FireStoreImagesField.FORESTRY_ID.tag] as String,
                            leaderId = data[FireStoreImagesField.LEADER_ID.tag] as String,
                            userId = data[FireStoreImagesField.USER_ID.tag] as String,
                            userRole = data[FireStoreImagesField.USER_ROLE.tag] as String,
                            imageUrl = data[FireStoreImagesField.IMAGE_URL.tag] as String,
                            thumbnailUrl = data[FireStoreImagesField.IMAGE_THUMBNAIL_URL.tag] as String,
                            woodType = data[FireStoreImagesField.WOOD_TYPE.tag] as String,
                            woodVolume = data[FireStoreImagesField.WOOD_VOLUME.tag] as Double,
                            woodLength = data[FireStoreImagesField.WOOD_LENGTH.tag] as Double,
                            location = data[FireStoreImagesField.LOCATION.tag] as GeoPoint
                        )
                        firestoreImageItems.add(firestoreImageItem)
                    }
                    cont.resume(firestoreImageItems)
                }
                .addOnFailureListener { exception ->
                    Log.w("TAG", "Error getting documents: ", exception)
                    cont.resumeWithException(exception)
                }
        }
    }

    suspend fun getUserDocumentById(userId: String): DocumentSnapshot = suspendCoroutine { cont ->
        val userDocumentReference = firestore.collection(
            FireStoreCollection.USER.tag
        ).document(userId)
        userDocumentReference.get()
            .addOnSuccessListener { cont.resume(it) }
            .addOnFailureListener { cont.resumeWithException(it) }
    }

    suspend fun getForestryDocumentById(forestryId: String): DocumentSnapshot =
        suspendCoroutine { cont ->
            val userDocumentReference = firestore.collection(
                FireStoreCollection.FORESTRY.tag
            ).document(forestryId)
            userDocumentReference.get()
                .addOnSuccessListener { cont.resume(it) }
                .addOnFailureListener { cont.resumeWithException(it) }
        }

    suspend fun getRoleDocumentByRef(roleRef: DocumentReference): DocumentSnapshot =
        suspendCoroutine { cont ->
            roleRef.get()
                .addOnSuccessListener { cont.resume(it) }
                .addOnFailureListener { cont.resumeWithException(it) }
        }
}
