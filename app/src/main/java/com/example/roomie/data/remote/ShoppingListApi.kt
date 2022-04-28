package com.example.roomie.data.remote

import com.example.roomie.domain.model.ShoppingList
import retrofit2.Response
import retrofit2.http.*

interface ShoppingListApi {

    @GET("/shopping")
    suspend fun getByIds(@Header("flatId") flatId: Int): Response<List<ShoppingList>>

    @GET("shopping/{shoppingListId}")
    suspend fun getById(@Path("shoppingListId") shoppingListId: Int): Response<ShoppingList>

    @POST("/shopping/{name}")
    suspend fun post(
        @Path("name") name: String,
        @Header("flatId") flatId: Int
    ): Response<ShoppingList>

    @PUT("/shopping/{shoppingListId}")
    suspend fun put(
        @Path("shoppingListId") shoppingListId: Int,
        @Header ("name") name: String
    ): Response<ShoppingList>

    @DELETE("/shopping/{shoppingListId}")
    suspend fun delete(@Path("shoppingListId") shoppingListId: Int): Response<Unit>
}