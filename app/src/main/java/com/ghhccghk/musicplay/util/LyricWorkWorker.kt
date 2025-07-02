package com.ghhccghk.musicplay.util

import android.content.Context
import androidx.compose.ui.unit.DpSize
import androidx.glance.GlanceId
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters


class WorkWorker(
    private val context: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters) {
    companion object {
        private val uniqueWorkName = WorkWorker::class.java.simpleName
        // 排队进行工作
        fun enqueue(context: Context, size: DpSize, glanceId: GlanceId, force: Boolean = false) {
            val manager = WorkManager.getInstance(context)
            val requestBuilder = OneTimeWorkRequestBuilder<WorkWorker>().apply {
                addTag(glanceId.toString())
                setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                setInputData(
                    Data.Builder()
                        .putFloat("width", size.width.value)
                        .putFloat("height", size.height.value)
                        .putBoolean("force", force)
                        .build()
                )
            }
            val workPolicy = if (force) {
                ExistingWorkPolicy.REPLACE
            } else {
                ExistingWorkPolicy.KEEP
            }
            manager.enqueueUniqueWork(
                uniqueWorkName + size.width + size.height,
                workPolicy,
                requestBuilder.build()
            )
        }
        /**
         * 取消任何正在进行的工作
         */
        fun cancel(context: Context, glanceId: GlanceId) {
            WorkManager.getInstance(context).cancelAllWorkByTag(glanceId.toString())
        }
    }
    override suspend fun doWork(): Result {
        // 需要执行的操作
        return Result.success()
    }
}
