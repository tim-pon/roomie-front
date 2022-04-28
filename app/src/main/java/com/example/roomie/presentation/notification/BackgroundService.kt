package com.example.roomie.presentation.notification

import android.app.Service
import android.content.Intent
import android.graphics.*
import android.net.ConnectivityManager
import android.os.Handler
import android.os.IBinder
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.example.roomie.R
import com.example.roomie.core.Constants
import com.example.roomie.core.ServiceModule
import com.example.roomie.core.sharedpreferences.FlatStorage
import com.example.roomie.core.sharedpreferences.SecureStorage
import com.example.roomie.data.remote.ActivityApi
import com.example.roomie.domain.model.Activity
import com.example.roomie.presentation.home.activity.ActivityMessages
import retrofit2.Call
import retrofit2.Response
import java.text.SimpleDateFormat
import java.time.*
import java.util.*


/**
 * background service to performs an operation that isn't directly noticed by the user in order to notify the user about certain action
 * since the system imposes restrictions on running background services in API level 26 or higher, the background service acts like a foreground service that is noticeable to the user
 */
class BackgroundService : Service() {

    private val TAG = BackgroundService::class.java.simpleName

    /**
     * starts with 2 because the first notification has the id 1 (it't the notification that shows the user that the service is running)
     */
    private var notificationCounter = 2

    /**
     * sometimes the same activity is displayed several times as notification, this is because the times on the end devices are somewhat behind the actual time.
     * So the last id of the activities must be saved in a list to check if they have already been displayed to the user.
     */
    private var activityList: MutableList<Int> = mutableListOf<Int>()

    /**
     * to perform one-time setup procedures when the service is initially created
     */
    override fun onCreate() {
        super.onCreate()
    }

    /**
     * requests that the service be started. When this method executes, the service is started and can run in the background indefinitely
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        /**
         * Foreground services must display a Notification in order to act as one (has the id 1)
         */
        val notification = NotificationMessage.createNewNotification(
            this,
            "Service Running",
            "check if new messages are available",
            R.drawable.ic_roomie,
        )

        /**
         * usual way to create a foreground service was to create a background service, then promote that service to the foreground
         * since Android 8.0 this is no longer possible
         * now the app has five seconds to call the service's startForeground() method to show the new service's user-visible notification
         */
        startForeground(1, notification)

        /**
         * start the polling service to get notifications
         */
        serverRequest()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun serverRequest() {
        /**
         * build loop for the polling Service
         */
        val handler = Handler()
        var time = "${OffsetDateTime.now(ZoneOffset.UTC)}"

        /**
         * polling Service to get new data (1min intervall)
         * uses it's own thread to perform any actions
         */
        val runnable: Runnable = object : Runnable {

            override fun run() {
                Log.i(TAG, "is running")
                if (isNetworkAvailable()) {
                    try {
                        val flatId = FlatStorage.getFlatId() ?: 0
                        getActivity(flatId, time)
                        /**
                         * delay between the requests
                         */
                        time = "${OffsetDateTime.now(ZoneOffset.UTC)}"
                        handler.postDelayed(this, 10000)
                    } catch (e: Exception) {
                        handler.postDelayed(this, 120000)
                    }
                } else {
                    Log.e(TAG, "Error no internet connection")
                }
            }
        }
        /**
         * trigger the polling service for the first time
         */
        handler.post(runnable)
    }

    /**
     * check if phone has connection to the internet
     */
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    /**
     * get new about one flat
     * @param flatId to get the news of the related flat
     */
    private fun getActivity(flatId: Int, time: String) {
        /**
         * normal retrofit api call
         */
        val service = ServiceModule.buildService(ActivityApi::class.java)
        val call = service.getLastActivities(flatId, time)

        Log.i(TAG, "current UTC time: $time")
        val callback = object :
            retrofit2.Callback<List<Activity>?> {
            override fun onResponse(
                call: Call<List<Activity>?>,
                response: Response<List<Activity>?>,
            ) {
                if (response.isSuccessful) {
                    /**
                     * to go throw list of every new activity
                     */
                    Log.i(TAG, "Successfully")
                    response.body()
                        ?.forEach { activity: Activity ->
                            /**
                             * checks if notification is send from the own user
                             * if so then don't show the notification
                             */
                            val id = SecureStorage.getUserId()
                            if (activity.userId != id) {
                                /**
                                 * checks if activity was already displayed to the user
                                 */
                                if (!activityList.contains(activity.id)) {
                                    activityList.add(activity.id)
                                    createNotification(activity, notificationCounter)
                                    notificationCounter += 1
                                }
                            }
                        }
                } else {
                    Log.e(TAG, "not successful. ${response.code()}")
                }
            }

            override fun onFailure(
                call: Call<List<Activity>?>,
                t: Throwable,
            ) {
                Log.e(TAG, "error")
            }
        }
        /**
         * send the request and notify callback of its response
         */
        call.enqueue(callback)
    }

    /**
     * create nofification and get user image with glide for every activity
     * @param activity to get all infos from the activity to display it in notification
     * @param counter to set the custom notification id
     */
    fun createNotification(activity: Activity, counter: Int) {
        /**
         * LazyHeader with bearer token is required to fetch images from an protected api
         */
        val lazyHeaders = LazyHeaders.Builder()
            .addHeader("Authorization", "Bearer ${SecureStorage.getAuthToken()}")
            .build()

        /**
         * get user image via glide
         * By default Glide uses a custom HttpUrlConnection so it does not require any okhttp elements
         */
        val url = Constants.BASE_URL + "user/image/${activity.userId}"
        Glide.with(this@BackgroundService)
            .asBitmap()
            .load(GlideUrl(url, lazyHeaders))
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .placeholder(R.drawable.ic_account)
            .into(object : SimpleTarget<Bitmap>(50, 50) {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    /**
                     * create Notification message
                     */
                    NotificationMessage.createNewNotification(
                        this@BackgroundService,
                        activity.username,
                        ActivityMessages.getActivityItemDescription(activity,
                            this@BackgroundService),
                        counter,
                        R.drawable.ic_message,
                        resource
                    )
                }
            })
    }
}