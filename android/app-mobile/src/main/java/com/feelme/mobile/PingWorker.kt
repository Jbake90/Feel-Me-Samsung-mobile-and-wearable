package com.feelme.mobile

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.delay

class PingWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val partnerId = inputData.getString(KEY_PARTNER_ID) ?: return Result.failure()
        // TODO: Replace with real HTTPS call to your backend (Firebase Function, etc.)
        delay(500) // Simulate network
        return Result.success()
    }

    companion object {
        const val KEY_PARTNER_ID = "partnerId"
        const val KEY_WORK_ID = "workId"
    }
}
