package com.example.roomie.data.repository

import android.util.Log
import androidx.lifecycle.liveData
import com.example.roomie.core.*
import com.example.roomie.data.remote.FlatApi
import com.example.roomie.data.remote.UserApi
import com.example.roomie.domain.model.ChangePassword
import com.example.roomie.domain.model.Flat
import com.example.roomie.domain.model.User
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import java.lang.Exception
import javax.inject.Inject

class SettingsRepository @Inject constructor(
    private val apiUser: UserApi,
    private val apiFlat: FlatApi,
) {
    private val TAG = SettingsRepository::class.java.name

    fun getUser() = liveData<Resource<User>> {
        // posting data
        try {
            emit(Resource.loading(null))
            val apiResponse = apiUser.getUser()
            if (apiResponse.isSuccessful) {
                Log.i(TAG, "getUser Api call successful!")
                emit(Resource.success(apiResponse.body()))
            } else {
                if (apiResponse.code() in (400..599))
                    emit(Resource.apiError(apiResponse.code(), apiResponse.code().toString(), null))
            }
        } catch (e: Exception) {
            Log.e(TAG, "getUser call successful!")
        }
    }

    fun getFlatsFromUser() = liveData<Resource<List<Flat>>> {
        try {
            emit(Resource.loading(null))
            val apiResponse = apiUser.getUserFlats()
            if (apiResponse.isSuccessful) {
                Log.i(TAG, "getFlatsFromUser Api call successful!")
                emit(Resource.success(apiResponse.body()))
            } else {
                if (apiResponse.code() in (400..599))
                    emit(Resource.apiError(apiResponse.code(), apiResponse.code().toString(), null))
            }
        } catch (e: Exception) {
            Log.e(TAG, "getFlatsFromUser call successful!")
        }
    }

    fun getFlatInfo(flatId: Int) = liveData<Resource<Flat>> {
        try {
            // posting data
            emit(Resource.loading(null))
            val apiResponse = apiFlat.infoFlat(flatId)
            if (apiResponse.isSuccessful) {
                Log.i(TAG, "getFlatInfo call successful!")
                emit(Resource.success(apiResponse.body()))
            } else {
                if (apiResponse.code() in (400..599))
                    emit(Resource.apiError(apiResponse.code(), apiResponse.code().toString(), null))
            }
        } catch (e: Exception) {
            Log.e(TAG, "getFlatInfo call successful!")
        }
    }

    fun changePassword(changePassword: ChangePassword) = liveData<Resource<Long>> {
        try {
            // posting data
            emit(Resource.loading(null))
            val apiResponse = apiUser.changePassword(changePassword)
            if (apiResponse.isSuccessful) {
                Log.i(TAG, "changePassword call successful!")
                emit(Resource.success(apiResponse.code().toLong()))
            } else {
                if (apiResponse.code() in (400..599))
                    emit(Resource.apiError(apiResponse.code(),
                        apiResponse.errorBody().toString(),
                        0))
            }
        } catch (e: Exception) {
            Log.e(TAG, "changePassword call successful!")
        }
    }

    fun changeUsername(username: String) = liveData<Resource<Long>> {
        try {
            // posting data
            emit(Resource.loading(null))
            val apiResponse = apiUser.changeUsername(username)
            if (apiResponse.isSuccessful) {
                Log.i(TAG, "changeUsername call successful!")
                emit(Resource.success(apiResponse.code().toLong()))
            } else {
                if (apiResponse.code() in (400..599))
                    emit(Resource.apiError(apiResponse.code(),
                        apiResponse.errorBody().toString(),
                        0))
            }
        } catch (e: Exception) {
            Log.e(TAG, "changeUsername call successful!")
        }
    }

    fun uploadImage(image: MultipartBody.Part) = liveData<Resource<ResponseBody>> {
        try {
            emit(Resource.loading(null))
            val apiResponse = apiUser.uploadImage(image)
            if (apiResponse.isSuccessful) {
                Log.i(TAG, "uploadImage call successful! ${apiResponse.code()}")
                emit(Resource.success(apiResponse.body()))
            } else {
                if (apiResponse.code() in (400..599))
                    emit(Resource.apiError(apiResponse.code(), apiResponse.code().toString(), null))
            }
        } catch (e: Exception) {
            Log.e(TAG, "uploadImage call successful!")
        }
    }

}