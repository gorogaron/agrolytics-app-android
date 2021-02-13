package com.agrolytics.agrolytics_android.networking


import com.agrolytics.agrolytics_android.BuildConfig
import com.agrolytics.agrolytics_android.networking.model.ImageUploadRequest
import com.agrolytics.agrolytics_android.networking.model.ImageUploadResponse
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit


interface ApiService {
	//@POST("processImage")
	@POST("ww/process_auth")
	suspend fun uploadImage(@Body body: ImageUploadRequest): Response<ImageUploadResponse>

	companion object Factory {

		var userToken: String? = null

		private fun getBaseUrl(): String {
			return BuildConfig.BASE_URL
		}

		fun updateUserToken (newToken: String?){
			userToken = newToken
		}

		fun create(): ApiService {
			val header = Interceptor { chain ->
				val request = chain.request().newBuilder()
					.addHeader(
						"Content-Type",
						"application/json; charset=UTF-8"
					)
					.addHeader(
						"Authorization",
						"Bearer $userToken"
					)
					.addHeader(
						"Accept",
						"application/json"
					)
						.build()
				chain.proceed(request)
			}
			val client = OkHttpClient.Builder()
					.connectTimeout(15, TimeUnit.SECONDS)
					.callTimeout(15, TimeUnit.SECONDS)
					.writeTimeout(15, TimeUnit.SECONDS)
					.readTimeout(15, TimeUnit.SECONDS)
					.retryOnConnectionFailure(false)
					.addInterceptor(header)
					.addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
					.build()

			val retrofit = Retrofit.Builder()
					.client(client)
					.addConverterFactory(GsonConverterFactory.create())
					.baseUrl(getBaseUrl())
					.build()
			return retrofit.create(ApiService::class.java)
		}
	}
}