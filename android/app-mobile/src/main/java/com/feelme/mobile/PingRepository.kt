package com.feelme.mobile

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageClient.OnCompleteListener
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.tasks.await
import java.nio.charset.StandardCharsets
import java.util.UUID

class PingRepository(private val context: Context) {
    private val messageClient: MessageClient by lazy { Wearable.getMessageClient(context) }
    private val workManager: WorkManager by lazy { WorkManager.getInstance(context) }

    suspend fun pairWithPartner(partnerId: String): String {
        // TODO: replace with real backend call
        return if (partnerId.isNotBlank()) {
            "Paired with $partnerId (demo)"
        } else {
            "Partner ID required"
        }
    }

    suspend fun sendPing(partnerId: String): String {
        if (partnerId.isBlank()) return "Partner ID required"
        enqueueCloudPing(partnerId)
        sendWatchBridge("ping:$partnerId")
        return "Ping queued"
    }

    private fun enqueueCloudPing(partnerId: String) {
        val request = OneTimeWorkRequestBuilder<PingWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setInputData(
                Data.Builder()
                    .putString(PingWorker.KEY_PARTNER_ID, partnerId)
                    .putString(PingWorker.KEY_WORK_ID, UUID.randomUUID().toString())
                    .build()
            )
            .build()

        workManager.enqueueUniqueWork(
            "ping-$partnerId",
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            request
        )
    }

    private suspend fun sendWatchBridge(message: String) {
        val path = "/feelme/ping"
        val bytes = message.toByteArray(StandardCharsets.UTF_8)
        val nodes = Wearable.getNodeClient(context).connectedNodes.await()
        nodes.forEach { node ->
            messageClient.sendMessage(node.id, path, bytes)
        }
    }
}
