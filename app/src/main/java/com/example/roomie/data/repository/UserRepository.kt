package com.example.roomie.data.repository

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.liveData
import androidx.room.withTransaction
import com.example.roomie.core.Constants.CACHING_THRESHOLD
import com.example.roomie.core.Resource
import com.example.roomie.core.util.network.ReadNetworkBoundResource
import com.example.roomie.data.local.AppDatabase
import com.example.roomie.data.local.UserDao
import com.example.roomie.data.remote.UserApi
import com.example.roomie.domain.model.FlatUserCrossRef
import com.example.roomie.domain.model.User
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val api: UserApi,
    private val db: AppDatabase,
    private val dao: UserDao,
) {

    private val LOG_TAG = UserRepository::class.java.simpleName

    fun getUserInfosByFlatId(flatId: Int, forceFetch: Boolean) =
        object : ReadNetworkBoundResource<List<User>, List<User>>(LOG_TAG, "Users (by FlatId)") {

            override fun loadFromDb() = dao.getUserInfos()

            @SuppressLint("SimpleDateFormat")
            override fun shouldFetch(data: List<User>?): Boolean {
                if (forceFetch || data.isNullOrEmpty())
                    return true

                for (userInfo in data) {
                    if (System.currentTimeMillis() - userInfo.fetchedAt > CACHING_THRESHOLD)
                        return true
                }

                return false
            }

            override suspend fun fetchFromNetwork() = api.getUserInfosByFlatId(flatId)

            override suspend fun saveNetworkResult(item: List<User>) {
                db.withTransaction {
                    dao.deleteUsersNotInFlat(item.map { it.id!! })
                    dao.upsert(item)
                    dao.insertFlatUserCrossRefs(item.map { FlatUserCrossRef(flatId, it.id!!) })
                }
            }
        }.asLiveData()


    /**
     * to get user info by id
     * is different from the other request because this call only gets the data from the api and not the cache and also doesn't safe the results in the cache
     * @param flatId get status from this flat
     */
    fun getUserById(userId: Int) = liveData<Resource<User>> {
        try {
            emit(Resource.loading(null))
            val apiResponse = api.getUserById(userId)
            if (apiResponse.isSuccessful) {
                Log.i(LOG_TAG, "get User by Id call successful!")

                emit(Resource.success(apiResponse.body()))
            } else {
                if (apiResponse.code() in (400..599))
                    emit(Resource.apiError(apiResponse.code(), apiResponse.code().toString(), null))
            }
        } catch (e: Exception) {
            Log.e(LOG_TAG, "get User by Id call successful!")
        }
    }
}