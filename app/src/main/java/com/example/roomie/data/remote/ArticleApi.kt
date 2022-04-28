package com.example.roomie.data.remote

import com.example.roomie.domain.model.Article
import retrofit2.Response
import retrofit2.http.*

interface ArticleApi {

    @GET("/shopping/article")
    suspend fun getArticlesByShoppingListId(
        @Header("listId") shoppingListId: Int): Response<List<Article>>

    @GET("/shopping/article/{articleId}")
    suspend fun getArticleById(@Path("articleId") articleId: Int): Response<Article>

    @POST("/shopping/article")
    suspend fun post(
        @Header("flatId") flatId: Int,
        @Header("listId") shoppingListId: Int,
        @Body article: Article
    ): Response<Article>

    @PUT("/shopping/article")
    suspend fun put(@Body article: Article): Response<Article>

    @PUT("shopping/article/{articleId}")
    suspend fun tick(@Path ("articleId") articleId: Int): Response<Article>

    @DELETE("/shopping/article/{articleId}")
    suspend fun delete(@Path("articleId") articleId: Int): Response<Unit>
}