package com.feelme.wear

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.tasks.await
import java.nio.charset.StandardCharsets
import java.time.Instant

class WearPingClient(private val context: Context) : MessageClient.OnMessageReceivedListener {
    private val messageClient by lazy { Wearable.getMessageClient(context) }

    suspend fun sendPingFromWatch(): String {
        val nodes = Wearable.getNodeClient(context).connectedNodes.await()
        if (nodes.isEmpty()) return "No paired phone found"
        val payload = "ping:demo-partner-${'$'}{Instant.now().epochSecond}".toByteArray(StandardCharsets.UTF_8)
        nodes.forEach { node -> messageClient.sendMessage(node.id, DATA_PATH, payload) }
        return "Sent to phone"
    }

    override fun onMessageReceived(event: MessageEvent) {
        val message = event.data.toString(StandardCharsets.UTF_8)
        if (message.startsWith("ping:")) {
            val from = message.removePrefix("ping:")
            showNotification(from)
        }
    }

    fun register() {
        messageClient.addListener(this)
    }

    fun unregister() {
        messageClient.removeListener(this)
    }

    private fun showNotification(from: String) {
        val notification = NotificationCompat.Builder(context, WearApp.CHANNEL_ID)
            .setContentTitle("Feel Me")
            .setContentText("${'$'}from pinged you")
            .setSmallIcon(android.R.drawable.star_big_on)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(from.hashCode(), notification)
    }

    companion object {
        const val DATA_PATH = "/feelme/ping"
    }
}
