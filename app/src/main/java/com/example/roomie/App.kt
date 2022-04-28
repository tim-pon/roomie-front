package com.example.roomie

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.graphics.Color
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App: Application() {

    companion object {
        /**
         * @param BACKGROUND_SERVICE_CHANNEL to identify the notification channel
         */
        const val BACKGROUND_SERVICE_CHANNEL = "backgroundServiceChannel"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    /**
     * create one notification channel to send notifications to the android system
     */
    private fun createNotificationChannel() {
        /**
         * check Android versione (oreo or higher) - because NotificationChannel Class ist not avaible for lower api levels
         * register your app's notification channel
         * set the priority of the notifications
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // O stands for Ores or APi 26
            val notificationChannel = NotificationChannel(
                BACKGROUND_SERVICE_CHANNEL,
                "backgroundServiceChannel",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                enableVibration(true)
                description = "This is the Channel to reciever Messages in the Background"
            }
            val notificationManager = getSystemService(
                NotificationManager::class.java)
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }
}