package com.example.roomie.presentation.notification

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.roomie.App
import com.example.roomie.R
import com.example.roomie.presentation.MainActivity

object NotificationMessage {

    /**
     * create a custom notification to only display the background service activity
     * @param context allows access to application-specific resources, classes, ...
     * @param headline set headline of the ni
     * @param message to display a message for the user to get more info
     * @param smallIcon set an icon so the user can assign it more easily
     */
    fun createNewNotification(
        context: Context?,
        headline: String?,
        message: String?,
        smallIcon: Int
    ): Notification {

        /**
         * intent to get to the home menu of our app
         * @param flags define how the intent is handled (FLAG_ACTIVITY_NEW_TASK activity become the start of a new task on this history stack, FLAG_ACTIVITY_CLEAR_TASK any existing task that would be associated with the activity to be cleared before the activity is started)
         */
        val activityIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        /**
         * so that the notification can respond to a tap, usually to open an activity in your app that corresponds to the notification
         */
        val contentIntent =
            PendingIntent.getActivity(context, 0, activityIntent, PendingIntent.FLAG_IMMUTABLE)

        /**
         * register the notification on our channel and set style and behaviour on our notification
         */
        return NotificationCompat.Builder(
            context!!,
            App.BACKGROUND_SERVICE_CHANNEL
        )
            .setSmallIcon(smallIcon)
            .setContentTitle(headline)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setColor(R.style.Theme_Roomie)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .setProgress(100, 0, true)
            .setContentIntent(contentIntent)
            .build()
    }

    /**
     * create a custom notification for incoming messages
     * @param context allows access to application-specific resources, classes, ...
     * @param headline set headline of the ni
     * @param message to display a message for the user to get more info
     * @param id to identify the notification
     * @param smallIcon set an icon so the user can assign it more easily
     * @param picture set an image (user image)
     */
    fun createNewNotification(
        context: Context?,
        headline: String?,
        message: String?,
        id: Int,
        smallIcon: Int,
        picture: Bitmap,
    ) {
        val activityIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val contentIntent = PendingIntent.getActivity(context, 0, activityIntent, PendingIntent.FLAG_IMMUTABLE)

        val builder: NotificationCompat.Builder =
            NotificationCompat.Builder(
                context!!,
                App.BACKGROUND_SERVICE_CHANNEL
            )
                .setSmallIcon(smallIcon)
                .setLargeIcon(picture)
                .setContentTitle(headline)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setColor(R.style.Theme_Roomie)
                .setAutoCancel(true)
                .setOnlyAlertOnce(false)
                .setContentIntent(contentIntent)

        val notificationManagerCompat = NotificationManagerCompat.from(context)
        notificationManagerCompat.notify(id, builder.build())
    }
}