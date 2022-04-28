package com.example.roomie.data.remote

import com.example.roomie.data.remote.dto.TransactionDto
import com.example.roomie.domain.model.FinanceSettle
import com.example.roomie.domain.model.FinanceStatistics
import com.example.roomie.domain.model.Transaction
import retrofit2.Response
import retrofit2.http.*

interface TransactionApi {

    @GET("/finance/{transactionId}")
    suspend fun getTransactionWithUsersById(@Path("transactionId") id: Int): Response<TransactionDto>//TransactionWithUsers>

    @GET("/finance")
    suspend fun getTransactionsWithUsersByFlatId(@Header("flatId") flatId: Int): Response<List<TransactionDto>>

    @POST("/finance")
    suspend fun createTransactionWithUsersAndCreator(
        @Header("flatId") flatId: Int,
        @Body transactionDto: TransactionDto
    ): Response<Transaction>

    @PUT("/finance/{transactionId}")
    suspend fun put(
        @Path("transactionId") id: Int,
        @Body transactionDto: TransactionDto
    ): Response<TransactionDto>

    @DELETE("/finance/{transactionId}")
    suspend fun delete(@Path("transactionId") id: Int): Response<Unit>

    @GET("/finance/balances")
    suspend fun getFinanceStatistics(
        @Header("flatId") flatId: Int
    ): Response<List<FinanceStatistics>>

    @PUT("/finance")
    suspend fun settleTransactions(
        @Header("flatId") flatId: Int
    ): Response<List<FinanceSettle>>
}