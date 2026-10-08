package io.github.hosseinkarami_dev.penny

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit

private const val REMINDER_CHANNEL_ID = "penny_recurring_reminders"
private const val REMINDER_WORK_TAG = "penny-local-reminder"
private lateinit var reminderContext: Context
private var launchNotificationPermission: (() -> Unit)? = null

fun initializeLocalReminders(context: Context, requestPermission: () -> Unit) {
    reminderContext = context.applicationContext
    launchNotificationPermission = requestPermission
    val manager = reminderContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        manager.createNotificationChannel(NotificationChannel(REMINDER_CHANNEL_ID, "Recurring reminders", NotificationManager.IMPORTANCE_DEFAULT).apply {
            description = "Reminders for scheduled transactions"
        })
    }
}

actual fun requestLocalReminderPermission() {
    if (!::reminderContext.isInitialized) return
    if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(reminderContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
        launchNotificationPermission?.invoke()
    }
}

actual fun scheduleLocalReminder(id: String, title: String, message: String, atEpochMillis: Long) {
    if (!::reminderContext.isInitialized) return
    val delay = (atEpochMillis - System.currentTimeMillis()).coerceAtLeast(0L)
    val request = OneTimeWorkRequestBuilder<RecurringReminderWorker>()
        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
        .setInputData(workDataOf("title" to title, "message" to message, "notification_id" to id.hashCode()))
        .addTag(REMINDER_WORK_TAG)
        .build()
    WorkManager.getInstance(reminderContext).enqueueUniqueWork("penny-reminder-$id", ExistingWorkPolicy.REPLACE, request)
}

actual fun cancelAllLocalReminders() {
    if (::reminderContext.isInitialized) WorkManager.getInstance(reminderContext).cancelAllWorkByTag(REMINDER_WORK_TAG)
}

class RecurringReminderWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return Result.success()
        val title = inputData.getString("title") ?: return Result.failure()
        val message = inputData.getString("message").orEmpty()
        val notificationId = inputData.getInt("notification_id", id.hashCode())
        val notification = NotificationCompat.Builder(applicationContext, REMINDER_CHANNEL_ID)
            .setSmallIcon(applicationContext.applicationInfo.icon)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(applicationContext).notify(notificationId, notification)
        return Result.success()
    }
}
