package com.example.scanfit.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.scanfit.R
import com.example.scanfit.network.NetworkClient
import com.example.scanfit.network.RegisterDeviceTokenRequest
import com.example.scanfit.utils.ScanActivity
import com.example.scanfit.utils.SessionManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ScanFitMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val authToken = SessionManager(applicationContext).fetchAuthToken() ?: return
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                NetworkClient.userApiService.registerDeviceToken(
                    "Bearer $authToken",
                    RegisterDeviceTokenRequest(token = token)
                )
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val title = message.notification?.title ?: return
        val body = message.notification?.body ?: return
        showNotification(title, body)
    }

    private fun showNotification(title: String, body: String) {
        val channelId = "scanfit_reminders"
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Health Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            manager.createNotificationChannel(channel)
        }

        val intent = Intent(this, ScanActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
