package com.agrolytics.agrolytics_android.data.firebase

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.agrolytics.agrolytics_android.data.firebase.model.FireBaseStorageItem
import com.agrolytics.agrolytics_android.data.firebase.model.ImageDirectory
import com.agrolytics.agrolytics_android.utils.ImageUtils
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


class Storage {
    private val url: String = "gs://agrolytics-2d8ea.appspot.com"

    var storage: FirebaseStorage? = null

    init {
        storage = FirebaseStorage.getInstance(url)
    }

    suspend fun uploadToFireBaseStorage(fireBaseStorageItem: FireBaseStorageItem) {

        // Referenciák
        val maskRef = storage?.reference?.child(
            "${fireBaseStorageItem.forestryName}/masked/${fireBaseStorageItem.imageName}"
        )
        val thumbRef = storage?.reference?.child(
            "${fireBaseStorageItem.forestryName}/thumbnail/${fireBaseStorageItem.imageName}"
        )

        // Képek konvertálása bytearray-re
        val maskBytes = ImageUtils.getBytes(fireBaseStorageItem.maskedImage)
        val thumbBytes = ImageUtils.getBytes(fireBaseStorageItem.maskedImageThumbnail)

        uploadImageToStorage(maskRef!!, maskBytes)
        uploadImageToStorage(thumbRef!!, thumbBytes)
    }

    //TODO: Ha nincs net, ez a végtelenségig blokkolni fog...
    suspend fun deleteImage(forestryName : String, userId : String, timestamp : Long, directory: ImageDirectory) : Boolean = suspendCoroutine {cont->
        val pathReference = storage?.reference?.child("$forestryName/${directory.tag}/${userId}_$timestamp")
        pathReference?.delete()
            ?.addOnSuccessListener { cont.resume(true) }
            ?.addOnFailureListener { cont.resume(false) }
    }

    suspend fun downloadImage(forestryName : String, userId : String, timestamp : Long): Bitmap? = suspendCoroutine { cont ->
        val pathReference = storage?.reference?.child("$forestryName/masked/${userId}_$timestamp")
        pathReference?.getBytes(Long.MAX_VALUE)
            ?.addOnSuccessListener {
                cont.resume(BitmapFactory.decodeByteArray(it, 0, it.size))
            }
            ?.addOnFailureListener {
                cont.resumeWithException(it)
            }
    }

    private suspend fun uploadImageToStorage(reference: StorageReference, bytes: ByteArray) : Boolean = suspendCoroutine { cont ->
        reference.putBytes(bytes)
            .addOnSuccessListener {
                val metadata = StorageMetadata.Builder()
                    .setCacheControl("max-age=604800")
                    .build()
                reference.updateMetadata(metadata)
                    .addOnSuccessListener {
                        cont.resume(true)
                    }.addOnFailureListener{
                        cont.resumeWithException(it)
                    }
            }.addOnFailureListener {
                cont.resumeWithException(it)
            }
    }
}