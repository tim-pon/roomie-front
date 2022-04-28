package com.example.roomie.data.remote

import com.example.roomie.domain.model.Flat
import com.example.roomie.domain.model.User
import retrofit2.Response
import retrofit2.http.*

interface FlatApi {

    @GET("flat/info")
    suspend fun infoFlat(
        @Header("flatId") id: Int,
    ): Response<Flat>

    @POST("flat/{name}")
    suspend fun post(
        @Path("name") name: String
    ): Response<Flat>

    @POST("flat/join/{entryCode}")
    suspend fun joinFlat(
        @Path("entryCode") code: String
    ): Response<User>

    @PUT("flat/leave")
    suspend fun leaveFlat(
        @Header("flatId") id: Int
    ) : Response<User>

    @GET("flat/users")
    suspend fun getUsersByFlatId(@Header("flatId") flatId: Int): Response<List<User>>
}