package com.agrolytics.agrolytics_android.data.firestore

import android.net.Uri
import com.agrolytics.agrolytics_android.utils.ImageUtils
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import java.lang.Exception
import java.time.LocalDateTime
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


class FireBaseDataClient {

    private val url: String = "gs://webhost-7bf8f.appspot.com"

    var firestore: FirebaseFirestore? = null
    var storage: FirebaseStorage? = null

    init {
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance(url)
    }

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

    suspend fun uploadToFireBaseStorage(
        fireBaseStorageItem: FireBaseStorageItem
    ): Pair<Pair<String, String>, Pair<String, String>> {
        val imageName = LocalDateTime.now().toString()

        // Referenciák
        val maskRef = storage?.reference?.child(
            "${fireBaseStorageItem.forestryName}/masked/$imageName")
        val thumbRef = storage?.reference?.child(
            "${fireBaseStorageItem.forestryName}/thumbnail/$imageName")

        // Képek konvertálása bytearray-re
        val maskBytes = ImageUtils.getBytes(fireBaseStorageItem.maskedImage)
        val thumbBytes = ImageUtils.getBytes(fireBaseStorageItem.maskedImageThumbnail)

        // Thumbnailek feltöltése
        var maskUri: Uri?
        var thumbUri: Uri?
        try {
            maskUri = uploadImageToStorage(maskRef!!, maskBytes!!)
            thumbUri = uploadImageToStorage(thumbRef!!, thumbBytes!!)
        }
        catch (e: Exception) {
            // TODO: Handle Exception better
            maskUri = null
            thumbUri = null
        }
        if (maskUri != null && thumbUri != null) {
            return Pair(
                Pair(maskUri.toString(), maskRef!!.path),
                Pair(thumbUri.toString(), thumbRef!!.path)
            )
        }
        throw Exception()
    }

    fun downloadFromFireStore() : FireStoreImageItem {
        throw NotImplementedError()
    }
    fun downloadFromFireBaseStorage() : FireBaseStorageItem {
        throw NotImplementedError()
    }

    private suspend fun uploadImageToStorage(
        reference: StorageReference,
        bytes: ByteArray)
    : Uri = suspendCoroutine { cont ->
        reference.putBytes(bytes)
            .addOnSuccessListener { task ->
                val metadata = StorageMetadata.Builder()
                    .setCacheControl("max-age=604800")
                    .build()
                reference.updateMetadata(metadata)
                    .addOnSuccessListener {
                        task.storage.downloadUrl
                            .addOnSuccessListener { uri ->
                                cont.resume(uri)
                            }.addOnFailureListener {
                                cont.resumeWithException(it)
                            }
                    }.addOnFailureListener{
                        cont.resumeWithException(it)
                    }
            }.addOnFailureListener {
                cont.resumeWithException(it)
            }
    }

}