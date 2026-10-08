package io.github.hosseinkarami_dev.penny

import kotlin.time.Clock
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter

actual fun requestLocalReminderPermission() {
    UNUserNotificationCenter.currentNotificationCenter().requestAuthorizationWithOptions(
        UNAuthorizationOptionAlert or UNAuthorizationOptionBadge or UNAuthorizationOptionSound,
        completionHandler = { _, _ -> },
    )
}

actual fun scheduleLocalReminder(id: String, title: String, message: String, atEpochMillis: Long) {
    val seconds = ((atEpochMillis - Clock.System.now().toEpochMilliseconds()).coerceAtLeast(1_000L)) / 1_000.0
    val content = UNMutableNotificationContent().apply {
        setTitle(title)
        setBody(message)
        setSound(UNNotificationSound.defaultSound())
    }
    val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(seconds, repeats = false)
    val request = UNNotificationRequest.requestWithIdentifier("penny-reminder-$id", content, trigger)
    UNUserNotificationCenter.currentNotificationCenter().addNotificationRequest(request) { }
}

actual fun cancelAllLocalReminders() {
    UNUserNotificationCenter.currentNotificationCenter().removeAllPendingNotificationRequests()
}
