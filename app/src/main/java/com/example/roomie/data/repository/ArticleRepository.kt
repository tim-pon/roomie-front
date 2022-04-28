package com.example.roomie.data.repository

import androidx.lifecycle.asLiveData
import androidx.room.withTransaction
import com.example.roomie.core.Constants.CACHING_THRESHOLD
import com.example.roomie.core.Constants.TMP_ID
import com.example.roomie.core.util.network.ReadNetworkBoundResource
import com.example.roomie.core.util.network.writeNetworkBoundResource
import com.example.roomie.data.local.AppDatabase
import com.example.roomie.data.local.ArticleDao
import com.example.roomie.data.remote.ArticleApi
import com.example.roomie.domain.model.Article
import javax.inject.Inject

/**
 * Single source of truth for article data.
 * Methods of this class overrides Read- & WriteNetworkBoundResource.
 * Should only be called from ViewModels.
 * Retrieves data from cache and network.
 */
class ArticleRepository @Inject constructor(
    private val db: AppDatabase,
    private val dao: ArticleDao,
    private val api: ArticleApi
) {
    private val LOG_TAG = ArticleRepository::class.java.simpleName

    /**
     * Gets article by id.
     * @param id Article id.
     * @return onSuccess: LiveData<Resource<Article>>
     *         onError:   LiveData<Resource<(Api)/Error>>
     */
    fun getArticleById(id: Int, forceFetch: Boolean = false) =
        object : ReadNetworkBoundResource<Article, Article>(LOG_TAG, "article") {

            override fun loadFromDb() = dao.getArticle(id)

            override fun shouldFetch(data: Article?): Boolean {
                if (forceFetch || data == null)
                    return true

                return System.currentTimeMillis() - data.fetchedAt > CACHING_THRESHOLD
            }

            override suspend fun fetchFromNetwork() = api.getArticleById(id)

            override suspend fun saveNetworkResult(item: Article) {
                // Executes block as db transaction
                // On error no changes will be applied
                db.withTransaction { dao.upsert(item) }
            }
        }.asLiveData()


    /**
     * Gets all articles on shoppinglist
     * @param shoppingListId Id of the shoppinglist
     * @param forceFetch true:  forces network request
     *                   false: checks if cache is outdated
     * @return onSuccess: LiveData<Resource<List<Article>>>
     *         onError:   LiveData<Resource<(Api)/Error>>
     */
    fun getArticlesByShoppingListId(shoppingListId: Int, forceFetch: Boolean = false) =
        object : ReadNetworkBoundResource<List<Article>, List<Article>>(LOG_TAG, "articles") {

            override fun loadFromDb() = dao.getArticles(shoppingListId)

            override fun shouldFetch(data: List<Article>?): Boolean {
                if (forceFetch || data.isNullOrEmpty())
                    return true

                for (article in data) {
                    if (System.currentTimeMillis() - article.fetchedAt > CACHING_THRESHOLD)
                        return true
                }

                return false
            }

            override suspend fun fetchFromNetwork() = api.getArticlesByShoppingListId(shoppingListId)

            override suspend fun saveNetworkResult(item: List<Article>) {
                // Executes block as db transaction
                // On error no changes will be applied
                db.withTransaction {
                    dao.clearInvalidCache(shoppingListId, item.map { it.id })
                    dao.upsert(item)
                }
            }
        }.asLiveData()


    /**
     * Creates a new article on an shoppinglist
     * @param flatId FlatId of the shoppinglist
     * @param article Article to create
     * @return onSuccess: LiveData<Resource<Article>>
     *         onError:   LiveData<Resource<(Api)/Error>>
     */
    fun createArticle(flatId: Int, article: Article) =
        writeNetworkBoundResource(LOG_TAG, "article", "Inserting",
            writeLocal    = { dao.insert(article) },
            writeRemote   = { api.post(flatId, article.shoppingListId, article)},
            cacheResponse = {
                db.withTransaction {
                    dao.deleteArticleById(TMP_ID)
                    dao.insert(it)
                }
            }
        ).asLiveData()


    /**
     * Updates an article
     * @param article Article to update
     * @return onSuccess: LiveData<Resource<Article>>
     *         onError:   LiveData<Resource<(Api)/Error>>
     */
    fun setArticle(article: Article) =
        writeNetworkBoundResource(LOG_TAG, "article", "Updating",
            writeLocal    = { dao.update(article) },
            writeRemote   = { api.put(article) },
            cacheResponse = { dao.update(it) }
        ).asLiveData()


    /**
     * Deletes an article.
     * @param article Article to delete
     * @return onSuccess: LiveData<Resource<Unit>>
     *         onError:   LiveData<Resource<(Api)/Error>>
     */
    fun deleteArticle(article: Article) =
        writeNetworkBoundResource(LOG_TAG, "article", "Deleting",
            writeLocal          = { dao.delete(article) },
            writeRemote         = { api.delete(article.id)},
            shouldCacheResponse = { false },
            cacheResponse       = {}
        ).asLiveData()


    /**
     * Flips value of property bought.
     * @param article Article to flip value of
     * @return onSuccess: LiveData<Resource<Unit>>
     *         onError:   LiveData<Resource<(Api)/Error>>
     */
    fun tickArticle(article: Article) =
        writeNetworkBoundResource(LOG_TAG, "article", "Ticking",
            writeLocal    = { dao.update(article) },
            writeRemote   = { api.tick(article.id) },
            cacheResponse = { dao.update(article) }
        ).asLiveData()

}