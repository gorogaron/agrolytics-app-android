package com.agrolytics.agrolytics_android.network

import android.util.Log
import com.agrolytics.agrolytics_android.network.model.ImageUploadRequest
import com.agrolytics.agrolytics_android.network.model.ImageUploadResponse
import com.agrolytics.agrolytics_android.ui.login.LoginPresenter
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.withTimeout
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import retrofit2.Call
import retrofit2.HttpException
import retrofit2.Response
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AppServer : KoinComponent{
    private val auth = FirebaseAuth.getInstance()
    private val TAG = "AppServer"

    private suspend fun getUserToken() : String = withTimeout(10000){
        suspendCoroutine<String>{
            Log.d(TAG, "Updating user token ID")
            auth.currentUser?.getIdToken(false)
                    ?.addOnSuccessListener { userToken ->
                        Log.d(TAG, "User token ID updated successfully")
                        it.resume(userToken.token!!)
                    }
                    ?.addOnFailureListener { e ->
                        Log.d(TAG, "Couldn't update user token ID: $e")
                        it.resume("")
                    }
        }
    }

    suspend fun uploadImage(imageUploadRequest: ImageUploadRequest): Call<ImageUploadResponse> {
        val userToken = getUserToken()
        if (userToken == "") {
            //TODO: Jobb hibakezelés
            val errorResponse = "Error during user token ID update"
            val errorResponseBody = errorResponse.toResponseBody("application/json".toMediaTypeOrNull())
            val response = Response.error<String>(400, errorResponseBody)
            throw HttpException(response)
        }
        else {
            val apiService = ApiService.create(userToken)
            Log.d(TAG, "Started processing the image.")
            return apiService.uploadImage(imageUploadRequest)
        }
    }
}