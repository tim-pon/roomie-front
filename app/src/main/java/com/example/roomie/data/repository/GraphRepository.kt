package com.example.roomie.data.repository

import android.util.Log
import androidx.lifecycle.liveData
import com.example.roomie.core.Resource
import com.example.roomie.data.remote.GraphApi
import com.example.roomie.domain.model.MonthlyExpense
import javax.inject.Inject

class GraphRepository @Inject constructor(
    private val api: GraphApi,
) {
    private val TAG = GraphRepository::class.java.name

    fun getMonthlyExpenses(flatId: Int) = liveData<Resource<List<MonthlyExpense>>> {
        try {
            emit(Resource.loading(null))
            val apiResponse = api.getMonthlyExpenses(flatId)
            if (apiResponse.isSuccessful) {
                Log.i(TAG, "getMonthlyExpenses call successful!")
                emit(Resource.success(apiResponse.body()))
            } else {
                if (apiResponse.code() in (400..599))
                    emit(Resource.apiError(apiResponse.code(), apiResponse.code().toString(), null))
            }
        } catch (e: Exception) {
            Log.e(TAG, "getMonthlyExpenses call successful!")
        }
    }
}