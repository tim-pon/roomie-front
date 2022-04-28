package com.example.roomie.data.repository

import android.util.Log
import androidx.lifecycle.liveData
import com.example.roomie.core.sharedpreferences.FlatStorage
import com.example.roomie.core.Resource
import com.example.roomie.core.sharedpreferences.SecureStorage
import com.example.roomie.data.local.FlatDao
import com.example.roomie.data.remote.UserAuthenticationApi
import com.example.roomie.domain.model.User
import java.lang.Exception
import javax.inject.Inject

class AuthenticationRepository @Inject constructor(
    private val api: UserAuthenticationApi,
    private val flatDao: FlatDao
) {
    private val TAG = AuthenticationRepository::class.java.name

    fun login(user: User) = liveData<Resource<Long>> {
        try {
            // posting data
            emit(Resource.loading(null))
            val apiResponse = api.login(user)

            if (apiResponse.isSuccessful) {
                Log.i(TAG, "Login Api call successful!")
                val cookieList: List<String> = apiResponse.headers().values("Authorization")

                /**
                 * safe auth token in secure storage
                 */
                SecureStorage.setAuthToken(
                    cookieList[0],
                    apiResponse.body()?.id!!,
                    SecureStorage.AuthenticationState.AUTHENTICATED
                )

                val userWithFlats = apiResponse.body()!!
                val flatEntrys = userWithFlats.flats.size
                if (flatEntrys > 0) {
                    userWithFlats.flats[0].id?.let { FlatStorage.setFlatId(it) }
                    userWithFlats.flats[0].name?.let { FlatStorage.setFlatName(it) }

                    flatDao.insert(userWithFlats.flats)
                }
                emit(Resource.success(apiResponse.code().toLong()))
            } else {
                if (apiResponse.code() in (400..599))
                    emit(Resource.apiError(apiResponse.code(),
                        apiResponse.errorBody().toString(),
                        0))
            }
        } catch (e: Exception) {
            Log.e(TAG, "login call successful!")
        }
    }

    fun registration(user: User) = liveData<Resource<Long>> {
        try {
            emit(Resource.loading(null))
            val apiResponse = api.register(user)
            if (apiResponse.isSuccessful) {
                Log.i(TAG, "registration call successful!")
                val cookieList: List<String> = apiResponse.headers().values("Authorization")
                SecureStorage.setAuthToken(
                    cookieList[0],
                    apiResponse.body()?.id!!,
                    SecureStorage.AuthenticationState.AUTHENTICATED
                )
                emit(Resource.success(apiResponse.code().toLong()))
            } else {
                if (apiResponse.code() in (400..599))
                    emit(Resource.apiError(apiResponse.code(),
                        apiResponse.errorBody().toString(),
                        0))
            }
        } catch (e: Exception) {
            Log.e(TAG, "registration call successful!")
        }
    }

}