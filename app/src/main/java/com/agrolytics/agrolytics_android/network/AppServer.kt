package com.agrolytics.agrolytics_android.network

import com.agrolytics.agrolytics_android.network.model.ImageUploadRequest
import com.agrolytics.agrolytics_android.network.model.ImageUploadResponse
import retrofit2.Response

class AppServer {
    private var apiService = ApiService.create()

    suspend fun uploadImage(imageUploadRequest: ImageUploadRequest): Response<ImageUploadResponse>
            = apiService.uploadImage(imageUploadRequest)
}