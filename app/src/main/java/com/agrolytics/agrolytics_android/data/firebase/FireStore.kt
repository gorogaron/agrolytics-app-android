package com.agrolytics.agrolytics_android.data.firebase

import android.util.Log
import com.agrolytics.agrolytics_android.data.firebase.model.FireStoreCollection
import com.agrolytics.agrolytics_android.data.firebase.model.FireStoreImageItem
import com.agrolytics.agrolytics_android.data.firebase.model.FireStoreImagesField
import com.agrolytics.agrolytics_android.utils.SessionManager
import com.google.firebase.firestore.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class FireStore: KoinComponent {

    private val sessionManager: SessionManager by inject()

    var firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var fireStoreUpdateListener : ListenerRegistration

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

    suspend fun downloadFromFireStoreAfterTimestamp(timestamp: Long): List<FireStoreImageItem> {
        val firestoreImageItems = ArrayList<FireStoreImageItem>()
        return suspendCoroutine { cont ->
            firestore.collection(FireStoreCollection.IMAGES.tag)
                .whereEqualTo(FireStoreImagesField.USER_ID.tag, sessionManager.userId)
                .whereGreaterThan(FireStoreImagesField.TIMESTAMP.tag, timestamp)
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
                            woodType = data[FireStoreImagesField.WOOD_TYPE.tag] as String,
                            woodVolume = data[FireStoreImagesField.WOOD_VOLUME.tag] as Double,
                            woodLength = data[FireStoreImagesField.WOOD_LENGTH.tag] as Double,
                            location = data[FireStoreImagesField.LOCATION.tag] as GeoPoint,
                            firestoreId = document.id
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

    //TODO: Ha nincs net, ez a végtelenségig blokkolni fog...
    suspend fun deleteImage(firestoreId : String) : Boolean = suspendCoroutine {cont->
        val imageRef = firestore.collection(FireStoreCollection.IMAGES.tag).document(firestoreId)
        imageRef.delete()
            .addOnSuccessListener { cont.resume(true) }
            .addOnFailureListener { cont.resume(false) }
    }

    @ExperimentalCoroutinesApi
    fun listenForAddedItems(timestamp: Long) = callbackFlow {
        fireStoreUpdateListener = firestore.collection(FireStoreCollection.IMAGES.tag)
            .whereGreaterThan(FireStoreImagesField.TIMESTAMP.tag, timestamp)
            .whereEqualTo(FireStoreImagesField.USER_ID.tag, sessionManager.userId)
            .addSnapshotListener{ snapshots, e ->
                val firestoreImageItems = ArrayList<FireStoreImageItem>()
                for (documentChange in snapshots!!.documentChanges) {
                    when (documentChange.type) {
                        DocumentChange.Type.ADDED -> {
                            firestoreImageItems.add(FireStoreImageItem(documentChange.document))
                        }
                        DocumentChange.Type.REMOVED -> {
                            /**TODO: Ha fut az app és egy másik kliens töröl egy képet, akkor azt itt megkapjuk, nem kell lekérdezni a delted_images listenerben.*/
                        }
                        DocumentChange.Type.MODIFIED -> {
                            /**Ilyen elvileg nem történhet, adatot a szerveren nem tudunk módosítani*/
                        }
                    }
                }
                offer(firestoreImageItems)
            }
        awaitClose{ fireStoreUpdateListener.remove() }
    }

    fun stopListeners() {
        fireStoreUpdateListener.remove()
    }
}