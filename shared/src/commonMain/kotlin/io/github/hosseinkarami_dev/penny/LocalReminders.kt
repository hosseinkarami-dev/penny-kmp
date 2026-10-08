package io.github.hosseinkarami_dev.penny

expect fun requestLocalReminderPermission()
expect fun scheduleLocalReminder(id: String, title: String, message: String, atEpochMillis: Long)
expect fun cancelAllLocalReminders()
