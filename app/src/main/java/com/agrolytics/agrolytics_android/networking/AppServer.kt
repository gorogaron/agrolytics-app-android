package com.agrolytics.agrolytics_android.networking

import com.agrolytics.agrolytics_android.networking.model.ImageUploadRequest
import com.agrolytics.agrolytics_android.networking.model.ResponseImageUpload
import io.reactivex.Observable
import retrofit2.Response

class AppServer {

    private val apiService = ApiService.create()

    fun uploadImage(imageUploadRequest: ImageUploadRequest): Observable<Response<ResponseImageUpload>>
            = apiService.uploadImage(imageUploadRequest)
}