package com.example.roomie.data.repository

import androidx.lifecycle.asLiveData
import androidx.room.withTransaction
import com.example.roomie.core.Constants.CACHING_THRESHOLD
import com.example.roomie.core.Constants.TMP_ID
import com.example.roomie.core.util.network.ReadNetworkBoundResource
import com.example.roomie.core.util.network.writeNetworkBoundResource
import com.example.roomie.data.local.AppDatabase
import com.example.roomie.data.local.ShoppingListDao
import com.example.roomie.data.remote.ShoppingListApi
import com.example.roomie.domain.model.ShoppingList
import javax.inject.Inject

/**
 * Single source of truth for shoppinglist data.
 * Methods of this class overrides Read- & WriteNetworkBoundResource.
 * Should only be called from ViewModels.
 * Retrieves data from cache and network.
 */
class ShoppingListRepository @Inject constructor(
    private val db: AppDatabase,
    private val dao: ShoppingListDao,
    private val api: ShoppingListApi
) {
    private val LOG_TAG = ShoppingListRepository::class.java.simpleName

    /**
     * Gets shoppinglist by id.
     * @param id shoppinglist id.
     * @return onSuccess: Flow<Resource<ShoppingList>>
     *         onError:   Flow<Resource<(Api)/Error>>
     */
    fun getShoppingListById(id: Int, forceFetch: Boolean = false) =
        object : ReadNetworkBoundResource<ShoppingList, ShoppingList>(
                    LOG_TAG, "shoppinglist") {

            override fun loadFromDb() = dao.getShoppingList(id)

            override fun shouldFetch(data: ShoppingList?): Boolean {
                if (forceFetch || data == null)
                    return true

                return System.currentTimeMillis() - data.fetchedAt > CACHING_THRESHOLD
            }

            override suspend fun fetchFromNetwork() = api.getById(id)

            override suspend fun saveNetworkResult(item: ShoppingList) {
                // Executes block as db transaction
                // On error no changes will be applied
                db.withTransaction {
                    dao.upsert(item)
                }
            }
        }.asFlow()


    /**
     * Gets all shoppinglists of flat
     * @param flatId Id of flat
     * @param forceFetch true:  forces network request
     *                   false: checks if cache is outdated
     * @return onSuccess: LiveData<Resource<List<ShoppingList>>>
     *         onError:   LiveData<Resource<(Api)/Error>>
     */
    fun getShoppingListsByFlatId(flatId: Int, forceFetch: Boolean = false) =
        object : ReadNetworkBoundResource<List<ShoppingList>, List<ShoppingList>>(
                    LOG_TAG, "shoppinglists") {

            override fun loadFromDb() = dao.getShoppingLists(flatId)

            override fun shouldFetch(data: List<ShoppingList>?): Boolean {
                if (forceFetch || data.isNullOrEmpty())
                    return true

                for (shoppingList in data) {
                    if (System.currentTimeMillis() - shoppingList.fetchedAt > CACHING_THRESHOLD)
                        return true
                }

                return false
            }

            override suspend fun fetchFromNetwork() = api.getByIds(flatId)

            override suspend fun saveNetworkResult(item: List<ShoppingList>) {
                // Executes block as db transaction
                // On error no changes will be applied
                db.withTransaction {
                    dao.clearInvalidCache(flatId, item.map { it.id })
                    dao.upsert(item)
                }
            }

        }.asLiveData()


    /**
     * Creates a new shoppinglist for flat
     * @param shoppingList Shoppinglist to create
     * @return onSuccess: LiveData<Resource<ShoppingList>>
     *         onError:   LiveData<Resource<(Api)/Error>>
     */
//    fun createShoppingList(shoppingList: ShoppingList) =
//        object : WriteNetworkBoundResource<Long, ShoppingList>(
//                    LOG_TAG, "shoppinglist", "Adding") {
//
//            override suspend fun changeLocal() = dao.insertShoppingList(shoppingList)
//
//            override suspend fun changeRemote() = api.createShoppingList(shoppingList.name, shoppingList.flatId)
//
//            override suspend fun saveRemoteResponse(item: ShoppingList) {
//                // Executes block as db transaction
//                // On error no changes will be applied
//                db.withTransaction {
//                    dao.deleteShoppingListById(TMP_ID)
//                    dao.insertShoppingList(item)
//                }
//            }
//
//        }.asLiveData()

    fun createShoppingList(shoppingList: ShoppingList) =
        writeNetworkBoundResource(LOG_TAG, "shoppinglist", "Inserting",
            writeLocal    = { dao.insert(shoppingList) },
            writeRemote   = { api.post(shoppingList.name, shoppingList.flatId )},
            cacheResponse = {
                db.withTransaction {
                    dao.deleteShoppingListById(TMP_ID)
                    dao.insert(it)
                }
            }
        ).asLiveData()


    /**
     * Updates a shoppinglist
     * @param shoppingList Shoppinglist to update
     * @return onSuccess: LiveData<Resource<Unit>>
     *         onError:   LiveData<Resource<(Api)/Error>>
     */
//    fun setShoppingList(shoppingList: ShoppingList) =
//        object : WriteNetworkBoundResource<Unit, ShoppingList>(
//                    LOG_TAG, "shoppinglist", "Updating") {
//
//            override suspend fun changeLocal() = dao.setShoppingList(shoppingList)
//
//            override suspend fun changeRemote() = api.setShoppingList(shoppingList.id, shoppingList.name)
//
//            override suspend fun saveRemoteResponse(item: ShoppingList) = dao.setShoppingList(shoppingList)
//
//        }.asLiveData()
    fun setShoppingList(shoppingList: ShoppingList) =
        writeNetworkBoundResource(LOG_TAG, "shoppinglist", "Updating",
            writeLocal    = { dao.update(shoppingList) },
            writeRemote   = { api.put(shoppingList.id, shoppingList.name) },
            cacheResponse = { dao.update(it) }
        ).asLiveData()



    /**
     * Deletes a shoppinglist
     * @param shoppingList Shoppinglist to delete
     * @return onSuccess: LiveData<Resource<ShoppingList>>
     *         onError:   LiveData<Resource<(Api)/Error>>
     */
//    fun deleteShoppingList(shoppingList: ShoppingList) =
//        object : WriteNetworkBoundResource<Unit, Unit>(
//                    LOG_TAG, "shoppinglist", "Deleting") {
//
//            override suspend fun changeLocal() = dao.deleteShoppingList(shoppingList)
//
//            override suspend fun changeRemote() = api.deleteShoppingList(shoppingList.id)
//
//            // Can't save empty delete response to cache
//            override fun shouldSaveRemoteResponse(data: Unit?) = false
//
//            override suspend fun saveRemoteResponse(item: Unit) {}
//
//        }.asLiveData()
    fun deleteShoppingList(shoppingList: ShoppingList) =
        writeNetworkBoundResource(LOG_TAG, "shoppinglist", "Deleting",
            writeLocal          = { dao.delete(shoppingList) },
            writeRemote         = { api.delete(shoppingList.id) },
            shouldCacheResponse = { false },
            cacheResponse       = {}
        ).asLiveData()

}
