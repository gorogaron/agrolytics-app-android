package com.agrolytics.agrolytics_android.networking

import com.agrolytics.agrolytics_android.BuildConfig
import com.agrolytics.agrolytics_android.networking.model.ImageUploadRequest
import com.agrolytics.agrolytics_android.networking.model.ImageUploadResponse
import io.reactivex.Observable
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit
import com.agrolytics.agrolytics_android.utils.SessionManager
import org.koin.android.ext.android.inject

interface ApiService {
	//@POST("processImage")
	@POST("ww/process")
	fun uploadImage(@Body body: ImageUploadRequest): Observable<Response<ImageUploadResponse>>

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
					.callTimeout(10, TimeUnit.SECONDS)
					.addInterceptor(header)
					.addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
					.build()

			val retrofit = Retrofit.Builder()
					.client(client)
					.addCallAdapterFactory(RxJava2CallAdapterFactory.create())
					.addConverterFactory(GsonConverterFactory.create())
					.baseUrl(getBaseUrl())
					.build()
			return retrofit.create(ApiService::class.java)
		}
	}
}