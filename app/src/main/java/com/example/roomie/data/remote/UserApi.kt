package com.example.roomie.data.remote

import com.example.roomie.domain.model.*
import com.example.roomie.domain.model.ChangePassword
import com.example.roomie.domain.model.User
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface UserApi {

    @GET("user/flats")
    suspend fun getUserFlats(): Response<List<Flat>>

    @GET("user")
    suspend fun getUser(): Response<User>

    @PUT("user/password")
    suspend fun changePassword(
        @Body changePassword: ChangePassword
    ): Response<ResponseBody>

    @PUT("user/username/{username}")
    suspend fun changeUsername(
        @Path("username") username: String
    ): Response<User>

    @Multipart
    @POST("user/image")
    suspend fun uploadImage(
        @Part image: MultipartBody.Part
    ): Response<ResponseBody>

    @GET("user/image")
    suspend fun downloadImage(): Response<ResponseBody>

    @GET("user/image")
    fun downloadImageForNotification(): Call<ResponseBody>

    @GET("user/infos")
    suspend fun getUserInfosByFlatId(
        @Header("flatId") flatId: Int
    ): Response<List<User>>

    @GET("user/info/{id}")
    suspend fun getUserById(
        @Path("id") userId: Int
    ): Response<User>
}