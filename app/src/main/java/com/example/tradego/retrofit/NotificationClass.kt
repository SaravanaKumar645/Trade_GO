package com.example.tradego.retrofit

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build


open class NotificationClass : Application() {
   open val channel1="Trade Go cart"

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch1 =
                NotificationChannel(
                    channel1,
                    "Channel 1",
                    NotificationManager.IMPORTANCE_HIGH
                )
            ch1.description="Cart Notifications"
            val manager:NotificationManager= getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(ch1)
        }

    }
}