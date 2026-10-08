package io.github.hosseinkarami_dev.penny

import android.content.Context
import android.net.Uri

private lateinit var backupContext: Context
private var launchBackupSave: ((String) -> Unit)? = null
private var launchBackupOpen: (() -> Unit)? = null
private var pendingBackupContents: String? = null
private var pendingBackupResult: ((String?) -> Unit)? = null

fun initializeBackupFileAccess(context: Context, launchSave: (String) -> Unit, launchOpen: () -> Unit) {
    backupContext = context.applicationContext
    launchBackupSave = launchSave
    launchBackupOpen = launchOpen
}

fun completeBackupSave(uri: Uri?) {
    val contents = pendingBackupContents
    pendingBackupContents = null
    if (uri != null && contents != null) {
        runCatching { backupContext.contentResolver.openOutputStream(uri)?.bufferedWriter(Charsets.UTF_8)?.use { it.write(contents) } }
    }
}

fun completeBackupOpen(uri: Uri?) {
    val callback = pendingBackupResult
    pendingBackupResult = null
    val contents = uri?.let { selected -> runCatching { backupContext.contentResolver.openInputStream(selected)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() } }.getOrNull() }
    callback?.invoke(contents)
}

actual fun saveEncryptedBackup(contents: String) {
    check(launchBackupSave != null) { "Initialize backup file access before opening Penny." }
    pendingBackupContents = contents
    launchBackupSave?.invoke("penny-backup.pennybackup")
}

actual fun pickEncryptedBackup(onResult: (String?) -> Unit) {
    check(launchBackupOpen != null) { "Initialize backup file access before opening Penny." }
    pendingBackupResult = onResult
    launchBackupOpen?.invoke()
}
