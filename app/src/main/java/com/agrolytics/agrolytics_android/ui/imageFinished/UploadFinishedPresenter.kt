package com.agrolytics.agrolytics_android.ui.imageFinished

import android.graphics.Bitmap
import android.util.Log
import com.agrolytics.agrolytics_android.base.BasePresenter
import com.agrolytics.agrolytics_android.networking.model.ImageItem
import com.agrolytics.agrolytics_android.networking.model.ResponseImageUpload
import com.agrolytics.agrolytics_android.ui.imageFinished.fragment.UploadFinishedFragment
import com.agrolytics.agrolytics_android.utils.BitmapUtils
import com.agrolytics.agrolytics_android.utils.Util
import com.google.firebase.storage.StorageMetadata
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.doAsyncResult
import org.jetbrains.anko.uiThread
import java.io.File
import java.io.FileInputStream
import java.util.*

class UploadFinishedPresenter : BasePresenter<UploadFinishedScreen>() {

    val TAG = "UploadFinishedPresenter"

    private fun saveUploadedImageItem(
        responseImageUpload: ResponseImageUpload?,
        path: String?,
        fragment: UploadFinishedFragment,
        serverImage: String?,
        id: String?
    ) {
        val imageItem = ImageItem(
            id = id ?: (0..10000).random().toString(),
            localPath = path ?: "",
            isPushedToServer = true,
            latitude = Util.lat ?: 0.0,
            longitude = Util.long ?: 0.0,
            length = sessionManager?.length?.toDouble() ?: 0.0,
            volume = responseImageUpload?.result?.toDouble() ?: 0.0,
            time = Util.getCurrentDateString(),
            serverImage = serverImage
        )
        doAsync {
            roomModule?.database?.imageItemDao()?.addImage(imageItem)
            uiThread { screen?.updateView(fragment) }
        }
    }

    fun uploadImageToStorage(responseImageUpload: ResponseImageUpload?, path: String?, fragment: UploadFinishedFragment) {
        screen?.showLoading()

        val reference = fireStoreDB?.storage?.reference
        val file = File(path)
        val imageName = file.name
        val imageRef = reference?.child(imageName)

        sessionManager?.forestryID?.let { forestryID ->

            var forestryName = ""

            fireStoreDB?.db?.collection("forestry")?.document(forestryID)
                ?.get()
                ?.addOnSuccessListener {
                    forestryName = it["name"] as String

                    val forestryRef = reference?.child("$forestryName/masked/$imageName")
                    val thumbnailForestryRef = reference?.child("$forestryName/thumbnail/$imageName")
                    responseImageUpload?.image?.let { imageBase64 ->

                        val bitmap = BitmapUtils.getImage(imageBase64)
                        val bytes = BitmapUtils.getBytes(bitmap)

                        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 64, 48, true);
                        val resizedBytes = BitmapUtils.getBytes(resizedBitmap)

                        if (bytes != null && resizedBytes != null) {
                            val thumbnailUploadTask = thumbnailForestryRef?.putBytes(resizedBytes)
                            thumbnailUploadTask?.addOnSuccessListener {uploadThumbnailImageTaskSnapshot ->
                                uploadThumbnailImageTaskSnapshot.storage.downloadUrl.addOnSuccessListener { thumbnailUrl ->
                                    val metadata = StorageMetadata.Builder()
                                        .setCacheControl("max-age=604800")
                                        .build()
                                    thumbnailForestryRef!!.updateMetadata(metadata).addOnSuccessListener {
                                        val uploadTask = forestryRef?.putBytes(bytes)
                                        uploadTask?.addOnSuccessListener {uploadImageTaskSnapshot ->
                                            val metadata = StorageMetadata.Builder()
                                                .setCacheControl("max-age=604800")
                                                .build()
                                            forestryRef!!.updateMetadata(metadata).addOnSuccessListener { metadata ->
                                                // metadata.contentType should be null
                                                uploadImageTaskSnapshot.storage.downloadUrl.addOnSuccessListener { imageUri ->
                                                    Log.d(TAG, " downloadURL: $imageUri")
                                                    uploadImageToFireStore(
                                                        imageUri.toString(),
                                                        responseImageUpload,
                                                        path,
                                                        fragment,
                                                        "$forestryName/masked/$imageName",
                                                        "$forestryName/thumbnail/$imageName",
                                                        thumbnailUrl.toString()
                                                    )
                                                }.addOnFailureListener { error ->
                                                    handleAsyncError(error, responseImageUpload, path, fragment);
                                                }
                                            }.addOnFailureListener {error ->
                                                handleAsyncError(error, responseImageUpload, path, fragment);
                                            }
                                        }?.addOnFailureListener { error ->
                                            handleAsyncError(error, responseImageUpload, path, fragment);
                                        }
                                    }.addOnFailureListener { error ->
                                        handleAsyncError(error, responseImageUpload, path, fragment);
                                    }
                                }.addOnFailureListener {error ->
                                    handleAsyncError(error, responseImageUpload, path, fragment);
                                }
                            }?.addOnFailureListener {error ->
                                handleAsyncError(error, responseImageUpload, path, fragment);
                            }
                        }
                    }
                }
                ?.addOnFailureListener { it.printStackTrace() }
        } ?: run {
            screen?.showToast("Hiányzó erdész azonosító. Kérlek próbáld meg később.")
        }
    }

    private fun handleAsyncError(
        error: Exception,
        responseImageUpload: ResponseImageUpload?,
        path: String?,
        fragment: UploadFinishedFragment
    ) {
        saveUploadedImageItem(responseImageUpload, path, fragment, null, null)
        screen?.hideLoading()
        screen?.showToast("Upload failed, we saved it locally. You can try again in Images menu.")
        error.printStackTrace()
    }

    private fun uploadImageToFireStore(
        url: String?,
        responseImageUpload: ResponseImageUpload?,
        path: String?,
        fragment: UploadFinishedFragment,
        imageRef: String?,
        thumbnailRef: String?,
        thumbnailUrl: String?
    ) {
        val imageDocument = hashMapOf(
            "time" to Util.getCurrentDateString(),
            "lat" to Util.lat,
            "long" to Util.long,
            "role" to sessionManager?.userRole,
            "url" to url,
            //"volume" to (responseImageUpload?.result?.toDouble() ?: 1.0) * (sessionManager?.length?.toDouble() ?: 1.0),
            "volume" to responseImageUpload?.result?.toDouble(),
            "length" to sessionManager?.length,
            "imageRef" to imageRef,
            "userID" to sessionManager?.userID,
            "leaderID" to sessionManager?.leaderID,
            "forestryID" to sessionManager?.forestryID,
            "thumbnailRef" to thumbnailRef,
            "thumbnailUrl" to thumbnailUrl
        )

        fireStoreDB?.db?.collection("images")
            ?.add(imageDocument)
            ?.addOnSuccessListener { imageStored ->
                fireStoreDB?.db?.collection("images")?.document(imageStored.id)?.get()
                    ?.addOnSuccessListener {
                        screen?.hideLoading()
                        screen?.showToast("Upload finished")
                        screen?.updateView(fragment)
                        saveUploadedImageItem(responseImageUpload, path, fragment, it["url"] as String, imageStored.id)
                        Log.d(TAG, "DocumentSnapshot successfully written!")
                    }
                    ?.addOnFailureListener { e ->
                        saveUploadedImageItem(responseImageUpload, path, fragment, null,null)
                        screen?.hideLoading()
                        screen?.showToast("Upload failed, we saved it locally. You try again in Images menu.")
                        Log.w(TAG, "Error writing document", e)
                    }
            }
            ?.addOnFailureListener { e ->
                saveUploadedImageItem(responseImageUpload, path, fragment, null, null)
                screen?.hideLoading()
                screen?.showToast("Upload failed, we saved it locally. You try again in Images menu.")
                Log.w(TAG, "Error writing document", e)
            }

//        fireStoreDB?.db?.collection("images")?.document()
//            ?.set(imageDocument)
//            ?.addOnSuccessListener {
//                screen?.hideLoading()
//                screen?.showToast("Upload finished")
//                screen?.updateView(fragment)
//                saveUploadedImageItem(responseImageUpload, path, fragment)
//                Log.d(TAG, "DocumentSnapshot successfully written!")
//            }
//            ?.addOnFailureListener { e ->
//                saveUploadedImageItem(responseImageUpload, path, fragment, null)
//                screen?.hideLoading()
//                screen?.showToast("Upload failed, we saved it locally. You try again in Images menu.")
//                Log.w(TAG, "Error writing document", e)
//            }
    }

    fun deleteImageFromLocalDatabase(id: String?) {
        id?.let {
            doAsync {
                val imageItem = roomModule?.database?.imageItemDao()?.getImageById(id)
                imageItem?.let { roomModule?.database?.imageItemDao()?.deleteImage(imageItem) }
            }
        }
    }

}
