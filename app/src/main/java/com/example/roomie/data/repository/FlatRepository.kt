package com.example.roomie.data.repository

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.liveData
import androidx.room.withTransaction
import com.example.roomie.core.Constants.CACHING_THRESHOLD
import com.example.roomie.core.util.network.ReadNetworkBoundResource
import com.example.roomie.core.Resource
import com.example.roomie.core.sharedpreferences.FlatStorage
import com.example.roomie.data.local.AppDatabase
import com.example.roomie.data.local.FlatDao
import com.example.roomie.data.local.UserDao
import com.example.roomie.data.remote.FlatApi
import com.example.roomie.domain.model.Flat
import com.example.roomie.domain.model.User
import com.example.roomie.domain.model.*
import kotlinx.coroutines.flow.Flow
import java.lang.Exception
import javax.inject.Inject

class FlatRepository @Inject constructor(
    private val db: AppDatabase,
    private val flatDao: FlatDao,
    private val flatApi: FlatApi,
    private val userDao: UserDao,
) {
    private val LOG_TAG = FlatRepository::class.java.simpleName

    fun joinFlat(entryCode: String) = liveData<Resource<User>> {
        try {
            // posting data
            Log.i(LOG_TAG, entryCode)
            val apiResponse = flatApi.joinFlat(entryCode)
            if (apiResponse.isSuccessful) {
                Log.i(LOG_TAG, "joinFlat call successful!")
                /**
                 * save data in flat storage
                 */
                val flatId = apiResponse.body()?.flats?.get(0)?.id
                val flatName = apiResponse.body()?.flats?.get(0)?.name
                if (flatId != null && flatName != null) {
                    FlatStorage.setFlatId(flatId)
                    FlatStorage.setFlatName(flatName)
                }
                flatDao.insert(apiResponse.body()!!.flats)
                emit(Resource.success(apiResponse.body()))
            } else {
                if (apiResponse.code() in (400..599)) {
                    Log.e(LOG_TAG, apiResponse.toString())
                    emit(Resource.apiError(apiResponse.code(), apiResponse.code().toString(), null))
                }
            }
        } catch (e: Exception) {
            Log.e(LOG_TAG, "joinFlat call successful!")
        }
    }

    fun createFlat(flatName: String) = liveData<Resource<Flat>> {
        try {
            // posting data
            val apiResponse = flatApi.post(flatName)
            if (apiResponse.isSuccessful) {
                Log.i(LOG_TAG, "createFlat call successful!")
                flatDao.insert(apiResponse.body()!!)
                emit(Resource.success(apiResponse.body()))
            } else {
                if (apiResponse.code() in (400..599))
                    emit(Resource.apiError(apiResponse.code(), apiResponse.code().toString(), null))
            }
        } catch (e: Exception) {
            Log.e(LOG_TAG, "createFlat call successful!")
        }
    }

    fun leaveFlat(flatId: Int) = liveData<Resource<User>> {
        try {
            val apiResponse = flatApi.leaveFlat(flatId)
            if (apiResponse.isSuccessful) {
                Log.i(LOG_TAG, "leaveFlat Api call successful!")
                emit(Resource.success(apiResponse.body()))
            } else {
                if (apiResponse.code() in (400..599))
                    emit(Resource.apiError(apiResponse.code(), apiResponse.code().toString(), null))
            }
        } catch (e: Exception) {
            Log.e(LOG_TAG, "leaveFlat call successful!")
        }
    }


    fun getFlatInfo(flatId: Int, forceFetch: Boolean = false) =
        object : ReadNetworkBoundResource<Flat, Flat>(LOG_TAG, "flat") {

            override fun loadFromDb() = flatDao.getFlatById(flatId)

            @SuppressLint("SimpleDateFormat")
            override fun shouldFetch(data: Flat?): Boolean {
                if (forceFetch || data == null)
                    return true

                return System.currentTimeMillis() - data.fetchedAt > CACHING_THRESHOLD
            }

            override suspend fun fetchFromNetwork() = flatApi.infoFlat(flatId)

            override suspend fun saveNetworkResult(item: Flat) {
                db.withTransaction {
                    flatDao.upsert(item)
                }
            }

        }.asLiveData()


    fun getUsersByFlatId(flatId: Int, forceFetch: Boolean = false) =
        object : ReadNetworkBoundResource<FlatWithUsers, List<User>>(LOG_TAG, "flat with users") {

            override fun loadFromDb(): Flow<FlatWithUsers> {
                return userDao.getFlatWithUsers(flatId)
            }

            override fun shouldFetch(data: FlatWithUsers?): Boolean {
                if (forceFetch || data == null || data.users.isNullOrEmpty())
                    return true

                return System.currentTimeMillis() - data.flat.fetchedAt > CACHING_THRESHOLD
            }

            override suspend fun fetchFromNetwork() = flatApi.getUsersByFlatId(flatId)

            override suspend fun saveNetworkResult(item: List<User>) {
                db.withTransaction {
                    userDao.deleteUsersNotInFlat(item.map { it.id!! })
                    userDao.insert(item)
                    userDao.addUsersToFlat(item.map { FlatUserCrossRef(flatId, it.id!!) })
                }
            }
        }.asLiveData()

}