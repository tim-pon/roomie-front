package com.example.roomie.core

import com.example.roomie.core.sharedpreferences.SecureStorage
import com.example.roomie.core.util.network.LiveDataCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

/**
 * provide retrofit for background service because in service dagger inject won't work
 */
object ServiceModule {
    // Retrofit
    private val retrofit = Retrofit.Builder()
        .addConverterFactory(
            retrofit2.converter.moshi.MoshiConverterFactory.create(
                Moshi.Builder()
                    .add(KotlinJsonAdapterFactory())
                    .build()
            )
        )
        .addCallAdapterFactory(LiveDataCallAdapterFactory())
        .baseUrl(Constants.BASE_URL)
        .client(
            okhttp3.OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.MINUTES)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor { chain ->
                    var request = chain.request()
                    request = request.newBuilder().header("Authorization", "Bearer " + SecureStorage.getAuthToken()).build()
                    chain.proceed(request)
                }.build()
        )
        .build()

    fun<T> buildService(service: Class<T>): T{
        return retrofit.create(service)
    }
}