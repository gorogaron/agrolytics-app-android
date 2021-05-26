package com.agrolytics.agrolytics_android.network


import com.agrolytics.agrolytics_android.BuildConfig
import com.agrolytics.agrolytics_android.network.model.ImageUploadRequest
import com.agrolytics.agrolytics_android.network.model.ImageUploadResponse
import com.agrolytics.agrolytics_android.utils.SessionManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit


interface ApiService {
	//@POST("processImage")
	@POST("ww/process_auth")
	fun uploadImage(@Body body: ImageUploadRequest): Call<ImageUploadResponse>

	companion object Factory : KoinComponent{

		private fun getBaseUrl(): String {
			return BuildConfig.BASE_URL
		}

		fun create(token : String): ApiService {
			val header = Interceptor { chain ->
				val request = chain.request().newBuilder()
					.addHeader(
						"Content-Type",
						"application/json; charset=UTF-8"
					)
					.addHeader(
						"Authorization",
						"Bearer $token"
					)
					.addHeader(
						"Accept",
						"application/json"
					)
						.build()
				chain.proceed(request)
			}
			val client = OkHttpClient.Builder()
					.connectTimeout(60, TimeUnit.SECONDS)
					.callTimeout(60, TimeUnit.SECONDS)
					.writeTimeout(60, TimeUnit.SECONDS)
					.readTimeout(60, TimeUnit.SECONDS)
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