package com.example.roomie.data.repository

import android.util.Log
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import androidx.room.withTransaction
import com.example.roomie.core.Constants.CACHING_THRESHOLD
import com.example.roomie.core.Constants.TMP_ID
import com.example.roomie.core.Resource
import com.example.roomie.core.util.network.ReadNetworkBoundResource
import com.example.roomie.core.util.network.writeNetworkBoundResource
import com.example.roomie.data.local.AppDatabase
import com.example.roomie.data.local.TransactionDao
import com.example.roomie.data.local.UserDao
import com.example.roomie.data.remote.TransactionApi
import com.example.roomie.data.remote.dto.TransactionDto
import com.example.roomie.domain.model.*
import java.lang.Exception
import javax.inject.Inject

/**
 * Single source of truth for transaction data.
 * Methods of this class overrides Read- & WriteNetworkBoundResource.
 * Should only be called from ViewModels.
 * Retrieves data from cache and network.
 */
class TransactionRepository @Inject constructor(
    private val db: AppDatabase,
    private val dao: TransactionDao,
    private val api: TransactionApi,
    private val userDao: UserDao,
) {
    private val LOG_TAG = TransactionRepository::class.java.simpleName

    /**
     * Gets transaction with creator and users objects by it's id.
     * @param id transaction id.
     * @return onSuccess: LiveData<Resource<TransactionWithCreatorAndUsers>>
     *         onError:   LiveData<Resource<(Api)/Error>>
     */
    fun getTransactionWithCreatorAndUsersById(id: Int, forceFetch: Boolean = false) =
        object : ReadNetworkBoundResource<TransactionWithCreatorAndUsers,
                TransactionDto>(LOG_TAG, "Transaction") {

            override fun loadFromDb() = dao.getTransactionWithUsersById(id)

            override fun shouldFetch(data: TransactionWithCreatorAndUsers?): Boolean {
                if (forceFetch || data == null)
                    return true

                return System.currentTimeMillis() -
                        data.transactionWithCreator.transaction.fetchedAt > CACHING_THRESHOLD
            }

            override suspend fun fetchFromNetwork() = api.getTransactionWithUsersById(id)

            override suspend fun saveNetworkResult(item: TransactionDto) {
                // mapping dto to domain model
                val domain = item.mapToDomainModel()
                // preparing entries for mapper table
                val crossRef = domain.users?.map { user ->
                    TransactionUserCrossRef(
                        domain.transactionWithCreator.transaction.id!!,
                        user.id!!)
                }!!

                // Executes block as db transaction
                // On error no changes will be applied
                db.withTransaction {
                    dao.upsert(domain.transactionWithCreator.transaction)
                    userDao.upsert(domain.users!!)
                    userDao.insertFlatUserCrossRefs(domain.users!!.map {
                        FlatUserCrossRef(domain.transactionWithCreator.transaction.flatId!!, it.id!!)})
                    dao.insertTransactionUsersCrossRefs(crossRef)
                }
            }
        }.asLiveData()


    /**
     * Gets all transactions with creator and users objects of flat
     * @param flatId Id of flat
     * @param forceFetch true:  forces network request
     *                   false: checks if cache is outdated
     * @return onSuccess: LiveData<Resource<List<TransactionWithCreatorAndUsers>>>
     *         onError:   LiveData<Resource<(Api)/Error>>
     */
    fun getTransactionsWithCreatorAndUsersByFlatId(flatId: Int, forceFetch: Boolean = false) =
        object : ReadNetworkBoundResource<List<TransactionWithCreatorAndUsers>,
                List<TransactionDto>>(LOG_TAG, "transactions") {

            override fun loadFromDb() = dao.getTransactionsWithUsersByFlatId(flatId)

            override fun shouldFetch(data: List<TransactionWithCreatorAndUsers>?): Boolean {
                if (forceFetch || data.isNullOrEmpty())
                    return true

                data.forEach {
                    if (System.currentTimeMillis() -
                        it.transactionWithCreator.transaction.fetchedAt > CACHING_THRESHOLD)
                            return true
                }

                return false
            }

            override suspend fun fetchFromNetwork() = api.getTransactionsWithUsersByFlatId(flatId)

            override suspend fun saveNetworkResult(item: List<TransactionDto>) {

                // mapping dtos to domain models
                val domain = item.map { it.mapToDomainModel() }

                // getting users from each transaction
                // combining these lists (List<List<User>>) into one list with distinct users
                val users = domain.map { it.users!! }.flatten().distinctBy { it.id }

                // preparing entries for mapper table
                val crossRefs = domain.map { transactionWithCreatorAndUsers ->
                    transactionWithCreatorAndUsers.users?.map { user ->
                        TransactionUserCrossRef(
                            transactionWithCreatorAndUsers.transactionWithCreator.transaction.id!!,
                            user.id!!)}!!}.flatten()

                // Executes block as db transaction
                // On error no changes will be applied
                db.withTransaction {
                    dao.clearInvalidCache(flatId, item.map { it.id!! })
                    dao.upsert(domain.map { it.transactionWithCreator.transaction })
                    userDao.upsert(users)
                    userDao.insertFlatUserCrossRefs(users.map { user ->
                        FlatUserCrossRef(flatId,
                            user.id!!)})
                    dao.insertTransactionUsersCrossRefs(crossRefs)
                }
            }
        }.asLiveData()


    /**
     * Creates a new transaction for flat
     * @param transactionWithCreatorAndUsers transaction to create
     * @return onSuccess: LiveData<Resource<TransactionWithCreatorAndUsers>>
     *         onError:   LiveData<Resource<(Api)/Error>>
     */
    fun createTransactionWithCreatorAndUsers(
        transactionWithCreatorAndUsers: TransactionWithCreatorAndUsers
    ) = writeNetworkBoundResource(LOG_TAG, "transaction", "Inserting",
            writeLocal = {
                // preparing entries for mapper table
                val crossRef = transactionWithCreatorAndUsers.users?.map { user ->
                    TransactionUserCrossRef(
                        transactionWithCreatorAndUsers.transactionWithCreator.transaction.id!!,
                        user.id!!)}!!

                // Executes block as db transaction
                // On error no changes will be applied
                db.withTransaction {
                    dao.insert(transactionWithCreatorAndUsers.transactionWithCreator.transaction)
                    dao.insertTransactionUsersCrossRefs(crossRef)
                }
            },
            writeRemote = {
                api.createTransactionWithUsersAndCreator(
                    transactionWithCreatorAndUsers.transactionWithCreator.transaction.flatId!!,
                    transactionWithCreatorAndUsers.mapToDto())
            },
            cacheResponse = {
                // preparing entries for mapper table
                val crossRefs = transactionWithCreatorAndUsers.users?.map { user ->
                    TransactionUserCrossRef(it.id!!, user.id!!)}

                // Executes block as db transaction
                // On error no changes will be applied
                db.withTransaction {
                    dao.deleteTransactionById(TMP_ID)
                    dao.insert(it)
                    dao.insertTransactionUsersCrossRefs(crossRefs!!)
                }
            }
        ).asLiveData()


    /**
     * Updates a transaction
     * @param transactionWithCreatorAndUsers Transaction to delete
     * @return onSuccess: LiveData<Resource<TransactionWithCreatorAndUsers>>
     *         onError:   LiveData<Resource<(Api)/Error>>
     */
    fun setTransaction(transactionWithCreatorAndUsers: TransactionWithCreatorAndUsers) =
        writeNetworkBoundResource(LOG_TAG, "transaction", "Updating",
            writeLocal = {
                // Executes block as db transaction
                // On error no changes will be applied
                db.withTransaction {

                    // update existing transaction
                    dao.update(transactionWithCreatorAndUsers.transactionWithCreator.transaction)

                    // delete user transaction mapping for users not in the new transaction
                    dao.deleteCrossRefs(
                        transactionWithCreatorAndUsers.transactionWithCreator.transaction.id!!,
                        transactionWithCreatorAndUsers.users?.map { it.id!! }!!)

                    // insert user transaction mapping for users in the new transaction
                    dao.insertTransactionUsersCrossRefs(
                        transactionWithCreatorAndUsers.users?.map { user ->
                            TransactionUserCrossRef(
                                transactionWithCreatorAndUsers.transactionWithCreator.transaction.id!!,
                                user.id!!)}!!)
                }
            },
            writeRemote = {
                api.put(
                    transactionWithCreatorAndUsers.transactionWithCreator.transaction.id!!,
                    transactionWithCreatorAndUsers.mapToDto())
            },
            cacheResponse = {
                dao.update(it.mapToDomainModel().transactionWithCreator.transaction)
            }
        ).asLiveData()


    /**
     * Deletes a transaction
     * @param transactionWithCreatorAndUsers Transaction to delete
     * @return onSuccess: LiveData<Resource<TransactionWithCreatorAndUsers>>
     *         onError:   LiveData<Resource<(Api)/Error>>
     */
    fun deleteTransaction(transactionWithCreatorAndUsers: TransactionWithCreatorAndUsers) =
        writeNetworkBoundResource(LOG_TAG, "transaction", "Deleting",
            writeLocal = { dao.deleteTransactionById(
                transactionWithCreatorAndUsers.transactionWithCreator.transaction.id!!)
            },
            writeRemote = { api.delete(
                transactionWithCreatorAndUsers.transactionWithCreator.transaction.id!!)
            },
            shouldCacheResponse = { false },
            cacheResponse = {}
        ).asLiveData()


    /**
     * Get Finance status of each flat member
     * is different from the other request because this call only gets the data from the api and not the cache and also doesn't safe the results in the cache
     * @param flatId get status from this flat
     */
    fun getFinanceStatistics(flatId: Int) = liveData<Resource<List<FinanceStatistics>>> {
        try {
            emit(Resource.loading(null))
            val apiResponse = api.getFinanceStatistics(flatId)
            if (apiResponse.isSuccessful) {
                Log.i(LOG_TAG, "getFinanceStatistics call successful!")

                emit(Resource.success(apiResponse.body()))
            } else {
                if (apiResponse.code() in (400..599))
                    emit(Resource.apiError(apiResponse.code(), apiResponse.code().toString(), null))
            }
        } catch (e: Exception) {
            Log.e(LOG_TAG, "getFinanceStatistics call successful!")
        }
    }

    /**
     * settle Finance status of each flat member
     * is different from the other request because this call only gets the data from the api and not the cache and also doesn't safe the results in the cache
     * @param flatId get status from this flat
     */
    fun settleFinance(flatId: Int) = liveData<Resource<List<FinanceSettle>>> {
        try {
            emit(Resource.loading(null))
            val apiResponse = api.settleTransactions(flatId)
            if (apiResponse.isSuccessful) {
                Log.i(LOG_TAG, "settleFinance call successful!")

                emit(Resource.success(apiResponse.body()))
            } else {
                if (apiResponse.code() in (400..599))
                    emit(Resource.apiError(apiResponse.code(), apiResponse.code().toString(), null))
            }
        } catch (e: Exception) {
            Log.e(LOG_TAG, "settleFinance call successful!")
        }
    }

}