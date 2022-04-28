package com.example.roomie.di

import com.bumptech.glide.load.model.LazyHeaders
import com.example.roomie.core.Constants
import com.example.roomie.core.util.network.LiveDataCallAdapterFactory
import com.example.roomie.core.sharedpreferences.SecureStorage
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * This object contains methods to inject basic network functionality
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * Provides a singleton of LazyHeader used by glide.
     * LazyHeader with bearer token is required to fetch images from an protected api
     */
    @Provides
    @Singleton
    fun provideLazyHeader(): LazyHeaders =
        LazyHeaders.Builder()
            .addHeader("Authorization", "Bearer ${SecureStorage.getAuthToken()}")
            .build()

    /**
     * Provides a singleton HTTP client used by retrofit.
     * Adds bearer token to all requests.
     * Handles network timeouts etc.
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(1, TimeUnit.MINUTES)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                var request = chain.request()
                request = request.newBuilder().header("Authorization", "Bearer " + SecureStorage.getAuthToken()).build()
                chain.proceed(request)
            }.build()

    /**
     * Provides a singleton of MoshiConverterFactory used by retrofit to convert between objects and JSON
     */
    @Provides
    @Singleton
    fun provideConverterFactory(): MoshiConverterFactory =
        MoshiConverterFactory.create(
            Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()
        )

    /**
     * Provides a singleton of Retrofit used for api calls
     * @param converterFactory converts between objects and JSON
     * @param client HTTP client
     */
    @Provides
    @Singleton
    fun provideRetrofit(converterFactory: MoshiConverterFactory, client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .addConverterFactory(converterFactory)
            .addCallAdapterFactory(LiveDataCallAdapterFactory())
            .baseUrl(Constants.BASE_URL)
            .client(client)
            .build()
}