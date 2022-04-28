package com.example.roomie.data.local

import androidx.room.*
import com.example.roomie.domain.model.Article
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ArticleDao : BaseDao<Article> {

    @Query("SELECT * FROM articles WHERE id = :articleId")
    abstract fun getArticle(articleId: Int): Flow<Article>

    @Query("SELECT * FROM articles WHERE shoppingListId = :shoppingListId")
    abstract fun getArticles(shoppingListId: Int): Flow<List<Article>>

    @Query("DELETE FROM articles WHERE id = :articleId")
    abstract suspend fun deleteArticleById(articleId: Int)

    @Query("DELETE FROM articles WHERE shoppingListId = :shoppingListId AND id NOT IN (:ids)")
    abstract suspend fun clearInvalidCache(shoppingListId: Int, ids: List<Int>)

}