package com.feelme.wear

import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class WearFirebaseService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // TODO: forward to backend along with the paired phone
        Log.d(TAG, "Wear FCM token: $token")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val from = message.data["fromUserId"] ?: "Friend"
        val notification = NotificationCompat.Builder(this, WearApp.CHANNEL_ID)
            .setContentTitle("Feel Me")
            .setContentText("${'$'}from pinged you")
            .setSmallIcon(android.R.drawable.star_on)
            .build()
        NotificationManagerCompat.from(this).notify(from.hashCode(), notification)
    }

    companion object {
        private const val TAG = "WearFirebaseService"
    }
}
