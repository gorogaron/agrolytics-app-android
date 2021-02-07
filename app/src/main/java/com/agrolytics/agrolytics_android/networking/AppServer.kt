package com.agrolytics.agrolytics_android.networking

import com.agrolytics.agrolytics_android.networking.model.ImageUploadRequest
import com.agrolytics.agrolytics_android.networking.model.ImageUploadResponse
import io.reactivex.Observable
import retrofit2.Response

class AppServer {
    private var apiService = ApiService.create()

    fun getUserToken() = ApiService.userToken

    fun updateApiService(newToken: String?){
        ApiService.updateUserToken(newToken)
        apiService = ApiService.create()
    }

    fun uploadImage(imageUploadRequest: ImageUploadRequest): Observable<Response<ImageUploadResponse>>
            = apiService.uploadImage(imageUploadRequest)
}