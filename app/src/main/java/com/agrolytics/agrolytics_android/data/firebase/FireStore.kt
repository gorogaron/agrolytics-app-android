package com.agrolytics.agrolytics_android.data.firebase

import com.agrolytics.agrolytics_android.data.firebase.model.FireStoreCollection
import com.agrolytics.agrolytics_android.data.firebase.model.FireStoreImageItem
import com.agrolytics.agrolytics_android.data.firebase.model.FireStoreImagesField
import com.agrolytics.agrolytics_android.utils.SessionManager
import com.google.firebase.firestore.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class FireStore: KoinComponent {

    private val sessionManager: SessionManager by inject()

    var firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var fireStoreUpdateListener : ListenerRegistration
    private lateinit var fireStoreDeleteListener : ListenerRegistration

    suspend fun upload(fireStoreImageItem: FireStoreImageItem, collection: FireStoreCollection): String = suspendCoroutine { cont ->
        firestore.collection(collection.tag).add(fireStoreImageItem.toHashMap())
            .addOnSuccessListener {
                cont.resume(it.id)
            }
            .addOnFailureListener {
                cont.resumeWithException(it)
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

    suspend fun moveImageToDeletedCollection(fireStoreImageItem: FireStoreImageItem) : Boolean {
        val deleteResult = deleteImage(fireStoreImageItem.firestoreId)
        if (deleteResult) {
            upload(fireStoreImageItem, FireStoreCollection.DELETED_IMAGES)
            return true
        }
        return false
    }

    //TODO: listenForAddeditems és listenForDeletedItems egységesítése
    @ExperimentalCoroutinesApi
    fun listenForAddedItems(timestamp: Long) = callbackFlow {
        fireStoreUpdateListener = firestore.collection(FireStoreCollection.IMAGES.tag)
            .whereGreaterThan(FireStoreImagesField.TIMESTAMP.tag, timestamp)
            .whereEqualTo(FireStoreImagesField.USER_ID.tag, sessionManager.userId)
            .addSnapshotListener{ snapshots, e ->
                val firestoreImageItems = ArrayList<FireStoreImageItem>()
                for (documentChange in snapshots!!.documentChanges) {
                    if (!documentChange.document.metadata.hasPendingWrites()) {
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
                }
                if (firestoreImageItems.size > 0) offer(firestoreImageItems)
            }
        awaitClose{ fireStoreUpdateListener.remove() }
    }

    @ExperimentalCoroutinesApi
    fun listenForDeletedItems() = callbackFlow {
        fireStoreDeleteListener = firestore.collection(FireStoreCollection.DELETED_IMAGES.tag)
            .whereEqualTo(FireStoreImagesField.USER_ID.tag, sessionManager.userId)
            .addSnapshotListener{ snapshots, e ->
                val firestoreImageItems = ArrayList<FireStoreImageItem>()
                for (documentChange in snapshots!!.documentChanges) {
                    if (!documentChange.document.metadata.hasPendingWrites()) {
                        when (documentChange.type) {
                            DocumentChange.Type.ADDED -> {
                                firestoreImageItems.add(FireStoreImageItem(documentChange.document))
                            }
                        }
                    }
                }
                if (firestoreImageItems.size > 0) offer(firestoreImageItems)
            }
        awaitClose{ fireStoreDeleteListener.remove() }
    }

    fun stopListeners() {
        fireStoreUpdateListener.remove()
        fireStoreDeleteListener.remove()
    }
}