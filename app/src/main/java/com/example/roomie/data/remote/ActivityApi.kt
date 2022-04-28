package com.example.roomie.data.remote

import com.example.roomie.domain.model.Activity
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import java.sql.Timestamp
import java.time.LocalDateTime

interface ActivityApi {

    @GET("/activity")
    suspend fun getActivitiesByFlatId(@Header("flatId") flatId: Int): Response<List<Activity>>


    @GET("/activity/afterTime/{time}")
    fun getLastActivities(
        @Header("flatId") flatId: Int,
        @Path("time") time: String
    ): Call<List<Activity>>

}