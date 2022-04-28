package com.example.roomie.data.remote

import com.example.roomie.domain.model.MonthlyExpense
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface GraphApi {

    @GET("finance/expenses")
    suspend fun getMonthlyExpenses(@Header("flatId") flatId: Int): Response<List<MonthlyExpense>>

}