package com.childprotection.child.network

import com.childprotection.child.BuildConfig
import com.childprotection.child.data.SecurePrefs
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton Retrofit client with JWT auth interceptor.
 */
object ApiClient {

    private var securePrefs: SecurePrefs? = null
    private var apiService: ApiService? = null

    fun init(prefs: SecurePrefs) {
        securePrefs = prefs
    }

    fun getService(): ApiService {
        if (apiService == null) {
            val authInterceptor = Interceptor { chain ->
                val request = chain.request().newBuilder()
                securePrefs?.accessToken?.let { token ->
                    request.addHeader("Authorization", "Bearer $token")
                }
                chain.proceed(request.build())
            }

            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG)
                    HttpLoggingInterceptor.Level.BODY
                else
                    HttpLoggingInterceptor.Level.NONE
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(BuildConfig.API_BASE_URL + "/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            apiService = retrofit.create(ApiService::class.java)
        }
        return apiService!!
    }
}
