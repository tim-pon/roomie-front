package com.example.roomie.presentation.notification

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Handler
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.roomie.R
import com.example.roomie.core.ServiceModule
import com.example.roomie.core.sharedpreferences.FlatStorage
import com.example.roomie.data.remote.ActivityApi
import com.example.roomie.data.remote.UserApi
import com.example.roomie.di.AppModule
import com.example.roomie.domain.model.Activity
import com.example.roomie.presentation.home.activity.ActivityMessages
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import java.time.OffsetDateTime
import java.time.ZoneOffset

class BackgroundWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    private val context = context

    override fun doWork(): Result {
        serverRequest()
        return Result.success()
    }

    private fun serverRequest() {
        val flatId = FlatStorage.getFlatId()?: 0
        getActivity(flatId)
    }

    private fun getActivity(flatId: Int) {
        var service = ServiceModule.buildService(ActivityApi::class.java)
        val time = "${OffsetDateTime.now(ZoneOffset.UTC)}"
        var call = service.getLastActivities(flatId, time)
        Log.e("BackgroundService", "inside try with offsettime: $time")
        val callback = object :
            retrofit2.Callback<List<com.example.roomie.domain.model.Activity>?> {
            override fun onResponse(
                call: Call<List<Activity>?>,
                response: Response<List<Activity>?>,
            ) {
                if (response.isSuccessful) {

                    var counter = 0
                    response.body()
                        ?.forEach { activity: com.example.roomie.domain.model.Activity ->
                            Log.e("Background", "counter $counter")

                            val activity = response.body()!![counter]
                            getUserImage(activity, counter)
                            counter += 1

                        }
                } else {
                    Log.e("BackgroundService", "not successful. ${response.code()}")
                }
            }

            override fun onFailure(
                call: Call<List<Activity>?>,
                t: Throwable,
            ) {
                Log.e("BackgroundService", "error")
            }
        }
        call.enqueue(callback)
    }

    private fun getUserImage(activity: com.example.roomie.domain.model.Activity, counter: Int) {
        val imageService = ServiceModule.buildService(UserApi::class.java)
        val imageCall = imageService.downloadImageForNotification()
        val callbacks2 = object : retrofit2.Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>,
            ) {
                val bm = BitmapFactory.decodeStream(response.body()!!.byteStream())
                NotificationMessage.createNewNotification(
                    context,
                    activity.username,
                    ActivityMessages.getActivityItemDescription(activity, context),
                    counter,
                    R.drawable.ic_message,
                    bm
                )
            }

            override fun onFailure(
                call: Call<ResponseBody>,
                t: Throwable,
            ) {

            }

        }
        imageCall.enqueue(callbacks2)
    }
}