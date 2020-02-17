package com.agrolytics.agrolytics_android.networking

import com.agrolytics.agrolytics_android.networking.model.ImageUploadRequest
import com.agrolytics.agrolytics_android.networking.model.ResponseImageUpload
import com.agrolytics.agrolytics_android.utils.SessionManager
import io.reactivex.Observable
import org.koin.android.ext.android.inject
import retrofit2.Response

class AppServer {
    private var apiService = ApiService.create()

    fun updateApiService(newToken: String?){
        ApiService.updateUserToken(newToken)
        apiService = ApiService.create()
    }

    fun uploadImage(imageUploadRequest: ImageUploadRequest): Observable<Response<ResponseImageUpload>>
            = apiService.uploadImage(imageUploadRequest)
}