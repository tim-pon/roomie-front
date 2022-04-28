package com.example.roomie.data.remote

import com.example.roomie.domain.model.User
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface UserAuthenticationApi {

    @POST("user/login")
    suspend fun login(@Body userLogin: User): Response<User>

    @POST("user/register")
    suspend fun register(@Body userRegister: User): Response<User>

}