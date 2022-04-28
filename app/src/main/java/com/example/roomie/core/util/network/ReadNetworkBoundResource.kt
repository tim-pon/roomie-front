package com.example.roomie.core.util.network

import android.util.Log
import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import androidx.lifecycle.asLiveData
import com.example.roomie.core.Constants.HTTP_ERROR
import com.example.roomie.core.Constants.HTTP_SUCCESS
import com.example.roomie.core.Resource
import kotlinx.coroutines.flow.*
import retrofit2.Response

/**
 * Template class to reduce boilerplate for GET calls.
 * Standardized procedure to read cached data, fetch new data from network and update outdated cache.
 * Method asFlow holds the main logic of this class!
 *
 * Following methods MUST be implemented:
 *  - loadFromDb
 *  - fetchFromNetwork
 *  - saveNetworkResult
 *
 * Following methods CAN be implemented:
 *  - shouldFetch
 *  - onFetchFailed
 *  - processResponse
 */
abstract class ReadNetworkBoundResource<ResultType, RequestType>(
    private val logTag: String = ReadNetworkBoundResource::class.simpleName!!,
    private val resourceName: String="",
) {

    /**
     * Main logic of this class!
     * Abstract methods gets executed in following order
     *   1. loadFromDb
     *   2. shouldFetch
     *   3. fetchFromNetwork
     *   4. processResponse
     *   5. saveNetworkResult
     *   on Error: onFetchFailed
     *   last: loadFromDb
     */
    fun asFlow() = flow {
        emit(Resource.loading(null))

        // Loading from cache
        Log.i(logTag, "Loading $resourceName from cache...")
        val dbValue = loadFromDb().first()
        Log.d(logTag, "Cached $resourceName: $dbValue")
        if (shouldFetch(dbValue)) {
            Log.d(logTag, "Should fetch $resourceName: true")
            emit(Resource.loading(dbValue))

            try {
                // Loading from api
                Log.i(logTag, "Loading $resourceName from api...")
                val apiResponse = fetchFromNetwork()
                Log.d(logTag, "Api $resourceName: $apiResponse")
                Log.d(logTag, "Api Body $resourceName: ${apiResponse.body()}")

                when (apiResponse.code()) {
                    in HTTP_SUCCESS -> {
                        // Updating cache
                        Log.i(logTag, "Updating $resourceName cache...")
                        saveNetworkResult(processResponse(apiResponse)!!)
                        emitAll(loadFromDb().map { Resource.success(it) })
                    }
                    in HTTP_ERROR -> {
                        onFetchFailed()
                        emitAll(loadFromDb().map {
                            Resource.apiError(apiResponse.code(),
                                apiResponse.errorBody().toString(),
                                it)
                        })
                    }
                }
            } catch (e: Exception) {
                Log.e(logTag, "Error loading $resourceName")
                Log.e(logTag, e.localizedMessage?: "No Error Message")
                emit(Resource.apiError(408, e.localizedMessage?: "No Error Message", dbValue))
            }
        } else {
            emitAll(loadFromDb().map { Resource.success(it) })
        }
    }

    fun asLiveData() = asFlow().asLiveData()


    /**
     * MUST be implemented in subclasses for init read from local cache
     *
     * @return data from cache wrapped as flow
     */
    @MainThread
    protected abstract fun loadFromDb(): Flow<ResultType>


    /**
     * CAN be implemented in subclasses. Default return value is true.
     * In case you just want to load data from cache, you can skip the whole
     * network logic by overriding this method to return false.
     * Some of the network methods must be implemented with an empty body.
     *
     * @param data return value of changeRemote
     */
    protected open fun shouldFetch(data: ResultType?): Boolean = true


    /**
     * MUST be implemented in subclasses for reading data from network.
     *
     * @return api data wrapped in Response object
     */
    @MainThread
    protected abstract suspend fun fetchFromNetwork(): Response<RequestType>


    /** CAN be implemented in subclasses for additional custom error handling */
    protected open fun onFetchFailed() {}


    /**
     * CAN be implemented in subclasses for additional processing
     * of the api response before saving it.
     * Room calls are not allowed in MainThread --> WorkerThread
     *
     * @param response return value of changeRemote
     */
    @WorkerThread
    protected open fun processResponse(response: Response<RequestType>) = response.body()


    /**
     * MUST be implemented in subclasses for saving fetched data to cache.
     * Without overriding this function this class makes no sense!
     *
     * @param item return value of processResponse
     */
    @WorkerThread
    protected abstract suspend fun saveNetworkResult(item: RequestType)

}
