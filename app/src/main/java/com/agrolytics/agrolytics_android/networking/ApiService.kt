package com.agrolytics.agrolytics_android.networking

import com.agrolytics.agrolytics_android.BuildConfig
import com.agrolytics.agrolytics_android.networking.model.ImageUploadRequest
import com.agrolytics.agrolytics_android.networking.model.ResponseImageUpload
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

interface ApiService {

	@POST("process")
	fun uploadImage(@Body body: ImageUploadRequest): Observable<Response<ResponseImageUpload>>

	companion object Factory {
		private fun getBaseUrl(): String {
			return BuildConfig.BASE_URL
		}

		fun create(): ApiService {
			val header = Interceptor { chain ->
				val request = chain.request().newBuilder()
					.addHeader(
						"Content-Type",
						"application/json; charset=UTF-8"
					)
					.addHeader(
						"Accept",
						"application/json"
					)
						.build()
				chain.proceed(request)
			}
			val client = OkHttpClient.Builder()
					.readTimeout(30, TimeUnit.SECONDS)
					.connectTimeout(30, TimeUnit.SECONDS)
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