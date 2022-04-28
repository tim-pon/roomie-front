package com.example.roomie.data.repository

import android.annotation.SuppressLint
import androidx.room.withTransaction
import com.example.roomie.core.Constants
import com.example.roomie.core.util.network.ReadNetworkBoundResource
import com.example.roomie.data.local.ActivityDao
import com.example.roomie.data.local.AppDatabase
import com.example.roomie.data.remote.ActivityApi
import com.example.roomie.domain.model.Activity
import java.text.SimpleDateFormat
import javax.inject.Inject

class ActivityRepository @Inject constructor(
    private val db: AppDatabase,
    private val dao: ActivityDao,
    private val api: ActivityApi,
){

    fun getActivitiesByFlatId(flatId: Int, forceFetch: Boolean) =
    object : ReadNetworkBoundResource<List<Activity>, List<Activity>>() {

        override fun loadFromDb() = dao.getActivities(flatId)

        @SuppressLint("SimpleDateFormat")
        override fun shouldFetch(data: List<Activity>?): Boolean {
            if (forceFetch || data.isNullOrEmpty())
                return true

            for (activity in data) {
                val fetchDate = SimpleDateFormat(Constants.SQL_DATE_FORMAT).parse(activity.fetchedAt).time
                if (System.currentTimeMillis() - fetchDate > Constants.CACHING_THRESHOLD)
                    return true
            }

            return false
        }

        override suspend fun fetchFromNetwork() = api.getActivitiesByFlatId(flatId)

        override suspend fun saveNetworkResult(item: List<Activity>) {
            db.withTransaction {
                dao.deleteActivities(flatId)
                dao.insertActivities(item)
            }
        }
    }.asLiveData()
}