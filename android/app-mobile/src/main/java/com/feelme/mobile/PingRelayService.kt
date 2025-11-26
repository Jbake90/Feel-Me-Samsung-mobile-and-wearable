package com.feelme.mobile

import android.app.Notification
import android.app.NotificationManager
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.nio.charset.StandardCharsets

class PingRelayService : LifecycleService(), MessageClient.OnMessageReceivedListener {
    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    private val repository by lazy { PingRepository(applicationContext) }

    override fun onCreate() {
        super.onCreate()
        startForeground(1, buildNotification("Ready to relay pings"))
        Wearable.getMessageClient(this).addListener(this)
    }

    override fun onDestroy() {
        Wearable.getMessageClient(this).removeListener(this)
        job.cancel()
        super.onDestroy()
    }

    override fun onMessageReceived(event: MessageEvent) {
        val payload = event.data.toString(StandardCharsets.UTF_8)
        if (payload.startsWith("ping:")) {
            val partner = payload.removePrefix("ping:")
            scope.launch { repository.sendPing(partner) }
        }
    }

    private fun buildNotification(content: String): Notification {
        return NotificationCompat.Builder(this, FeelMeApp.NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Feel Me connected")
            .setContentText(content)
            .setSmallIcon(android.R.drawable.star_on)
            .setPriority(NotificationManager.IMPORTANCE_LOW)
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }
}
