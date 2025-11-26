package com.feelme.mobile

import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FirebasePingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // TODO: send token to backend for fan-out
        Log.d(TAG, "FCM token: $token")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val from = message.data["fromUserId"] ?: "Friend"
        val text = "${'$'}from is thinking of you"
        val notification = NotificationCompat.Builder(this, FeelMeApp.NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Feel Me")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.star_big_on)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(this).notify(text.hashCode(), notification)
    }

    companion object {
        private const val TAG = "FirebasePingService"
    }
}
