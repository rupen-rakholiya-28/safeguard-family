package com.childprotection.parent.network

import com.childprotection.parent.BuildConfig
import com.childprotection.parent.data.ParentPrefs
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private var prefs: ParentPrefs? = null
    private var service: ParentApiService? = null

    fun init(prefs: ParentPrefs) {
        this.prefs = prefs
    }

    fun getService(): ParentApiService {
        if (service == null) {
            val authInterceptor = Interceptor { chain ->
                val req = chain.request().newBuilder()
                prefs?.accessToken?.let { req.addHeader("Authorization", "Bearer $it") }
                chain.proceed(req.build())
            }

            val logging = HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                else HttpLoggingInterceptor.Level.NONE
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()

            service = Retrofit.Builder()
                .baseUrl(BuildConfig.API_BASE_URL + "/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ParentApiService::class.java)
        }
        return service!!
    }
}
