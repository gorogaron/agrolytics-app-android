package com.agrolytics.agrolytics_android.networking

import com.agrolytics.agrolytics_android.networking.model.ImageUploadRequest
import com.agrolytics.agrolytics_android.networking.model.ImageUploadResponse
import retrofit2.Response

class AppServer {
    private var apiService = ApiService.create()

    fun getUserToken() = ApiService.userToken

    fun updateApiService(newToken: String?){
        ApiService.updateUserToken(newToken)
        apiService = ApiService.create()
    }

    suspend fun uploadImage(imageUploadRequest: ImageUploadRequest): Response<ImageUploadResponse>
            = apiService.uploadImage(imageUploadRequest)
}