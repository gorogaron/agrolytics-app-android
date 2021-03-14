package com.agrolytics.agrolytics_android.data.firebase

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.agrolytics.agrolytics_android.data.firebase.model.FireBaseStorageItem
import com.agrolytics.agrolytics_android.utils.ImageUtils
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDateTime
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


class Storage {
    private val url: String = "gs://agrolytics-2d8ea.appspot.com"

    var storage: FirebaseStorage? = null

    init {
        storage = FirebaseStorage.getInstance(url)
    }

    suspend fun uploadToFireBaseStorage(
        fireBaseStorageItem: FireBaseStorageItem
    ): Pair<String, String> {
        val imageName = LocalDateTime.now().toString()

        // Referenciák
        val maskRef = storage?.reference?.child(
            "${fireBaseStorageItem.forestryName}/masked/$imageName"
        )
        val thumbRef = storage?.reference?.child(
            "${fireBaseStorageItem.forestryName}/thumbnail/$imageName"
        )

        // Képek konvertálása bytearray-re
        val maskBytes = ImageUtils.getBytes(fireBaseStorageItem.maskedImage)
        val thumbBytes = ImageUtils.getBytes(fireBaseStorageItem.maskedImageThumbnail)

        // Thumbnailek feltöltése
        var maskUri: Uri?
        var thumbUri: Uri?
        try {
            maskUri = uploadImageToStorage(maskRef!!, maskBytes)
            thumbUri = uploadImageToStorage(thumbRef!!, thumbBytes)
        }
        catch (e: Exception) {
            // TODO: Handle Exception better
            maskUri = null
            thumbUri = null
        }
        if (maskUri != null && thumbUri != null) {
            return Pair(maskUri.toString(), thumbUri.toString())

        }
        throw Exception()
    }

    //TODO: Ha nincs net, ez a végtelenségig blokkolni fog...
    suspend fun deleteImage(imageUrl: String) : Boolean = suspendCoroutine {cont->
        val imageRef = storage?.getReferenceFromUrl(imageUrl)
        imageRef?.delete()
            ?.addOnSuccessListener { cont.resume(true) }
            ?.addOnFailureListener { cont.resume(false) }
    }

    fun downloadImage(imageUrl: String?)
    : Bitmap? {
        val url = URL(imageUrl)
        val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
        connection.setDoInput(true)
        connection.connect()
        val input: InputStream = connection.getInputStream()
        return BitmapFactory.decodeStream(input)
    }

    private suspend fun uploadImageToStorage(
        reference: StorageReference,
        bytes: ByteArray
    )
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