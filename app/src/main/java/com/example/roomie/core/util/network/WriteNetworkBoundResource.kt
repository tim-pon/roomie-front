package com.example.roomie.core.util.network

import android.util.Log
import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import androidx.lifecycle.asLiveData
import com.example.roomie.core.Constants.HTTP_ERROR
import com.example.roomie.core.Constants.HTTP_SUCCESS
import com.example.roomie.core.Resource
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import retrofit2.Response

/**
 * Template class to reduce boilerplate for POST, PUT and DELETE calls.
 * Standardized procedure to perform api calls, save responses to local cache and handle errors.
 * Method asFlow holds the main logic of this class!
 *
 * Following methods MUST be implemented:
 *   - changeLocal
 *   - changeRemote
 *   - saveRemoteResponse
 *
 * Following methods CAN be implemented:
 *   - shouldChangeLocal
 *   - onFetchFailed
 *   - processResponse
 *   - shouldSaveRemoteResponse
 */
abstract class WriteNetworkBoundResource<ResultType, RequestType>(
    private val logTag: String = WriteNetworkBoundResource::class.java.simpleName,
    private val resourceName: String = "",
    private val action: String = "Writing"
) {

    /**
     * Main logic of this class!
     * Abstract methods gets executed in following order
     *   1. shouldChangeLocal
     *   2. changeLocal
     *   3. changeRemote
     *   4. shouldSaveRemoteResponse
     *   5. processResponse
     *   6. saveRemoteResponse
     *   on Error: onFetchFailed
     */
    fun asFlow() = flow {
        emit(Resource.loading(null))

        // should make init local change?
        if (shouldChangeLocal()) {
            // init local update
            Log.i(logTag, "$action $resourceName from/to cache ...")
            emit(Resource.loading(changeLocal()))
        }

//        try {
            // remote update
            Log.i(logTag, "$action $resourceName from/to api ...")
            val apiResponse = changeRemote()
            Log.v(logTag, apiResponse.toString())
            Log.v(logTag, apiResponse.body().toString())
            Log.v(logTag, apiResponse.errorBody().toString())

            when (apiResponse.code()) {
                in HTTP_SUCCESS -> {
                    if (shouldSaveRemoteResponse(apiResponse.body())) {
                        Log.i(logTag, "Updating $resourceName cache...")
                        emit(Resource.success(saveRemoteResponse(processResponse(apiResponse)!!)))
                    } else
                        emit(Resource.success(apiResponse))
                }
                in HTTP_ERROR -> {
                    onFetchFailed()
                    emit(Resource.apiError(apiResponse.code(),
                        apiResponse.errorBody().toString(),
                        apiResponse))
                }
            }
//        } catch (e: Exception) {
//            Log.e(logTag, e.toString())
//            emit(Resource.apiError(408, e.localizedMessage, null))
//        }
    }

    fun asLiveData() = asFlow().asLiveData()


    /**
     * CAN be implemented in subclasses. Default return value is true.
     * If you want to skip change local override this method to return false.
     */
    protected open fun shouldChangeLocal(): Boolean = true


    /**
     * MUST be implemented in subclasses for init local write
     * In case you don't want to initially write data to cache set shouldChangeLocal to false and
     * implement changeLocal with an empty body.
     * Room calls are not allowed in MainThread --> WorkerThread
     */
    @WorkerThread
    protected abstract suspend fun changeLocal(): ResultType


    /**
     * MUST be implemented in subclasses for writing data to remote.
     * Without overriding this function this class makes no sense!
     *
     * @return api data wrapped in Response object
     */
    @MainThread
    protected abstract suspend fun changeRemote(): Response<RequestType>


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
     * CAN be implemented in subclasses. Default return value is true.
     * If you want to skip saveRemoteResponse override this method to return false.
     *
     * @param data return value of changeRemote
     */
    protected open fun shouldSaveRemoteResponse(data: RequestType?): Boolean = true


    /**
     * MUST be implemented in subclasses for saving fetched data to cache.
     * Some Api responses like (204 No Content) can not be saved to cache.
     * In this case set shouldSaveRemoteResponse to true and implement
     * saveRemoteResponse with an empty body.
     *
     * @param item return value of processResponse
     */
    @WorkerThread
    protected abstract suspend fun saveRemoteResponse(item: RequestType)
}

// RequestType = Insert Response --> Long or Unit
// ResultType = Real Data --> Article
inline fun <ResultType, RequestType> writeNetworkBoundResource(
    LOG_TAG: String = "WriteNetworkBoundResource",
    resourceName: String = "",
    action: String = "Write",

    // should init write data to cache?
    crossinline shouldWriteLocal: () -> Boolean = { true },

    // write local data
    crossinline writeLocal: suspend () -> ResultType,

    // write network data
    crossinline writeRemote: suspend () -> Response<RequestType>,

    // should write network response to cache?
    crossinline shouldCacheResponse: (RequestType) -> Boolean = { true },

    // write network response to cache
    crossinline cacheResponse: suspend (RequestType) -> ResultType

) = flow {

    if (shouldWriteLocal()) {
        Log.v(LOG_TAG, "$action $resourceName from/to cache...")
        emit(Resource.loading(writeLocal()))
    }

    val flow = try {
        Log.v(LOG_TAG, "$action $resourceName from/to network...")
        val netData = writeRemote()
        when (netData.code()) {
            in HTTP_SUCCESS -> {
                if (shouldCacheResponse(netData.body()!!)) {
                    Log.v(LOG_TAG, "Updating $resourceName cache...")
                    flow { emit(Resource.success(cacheResponse(netData.body()!!))) }
                } else {
                    flow { emit(Resource.success(writeLocal())) }
                }
            }
            in HTTP_ERROR -> {
                flow { emit(Resource.apiError(netData.code(),
                    netData.errorBody().toString(),
                    netData))}
            } else -> {
            flow { emit(Resource.apiError(408, "Unknown network error", netData))}
        }
        }
    } catch (e: Exception) {
        flow { emit(Resource.error(e.toString(), writeLocal()))}
    }

    emitAll(flow)
}