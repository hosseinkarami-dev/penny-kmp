package io.github.hosseinkarami_dev.penny

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.usePinned
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.darwin.NSObject
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIApplication
import platform.UniformTypeIdentifiers.UTType
import platform.posix.fclose
import platform.posix.fread
import platform.posix.fseek
import platform.posix.fopen
import platform.posix.ftell
import platform.posix.rewind
import platform.posix.fwrite
import kotlinx.cinterop.refTo
import platform.posix.SEEK_END

private var pendingBackupImport: ((String?) -> Unit)? = null
private var activeBackupPickerDelegate: BackupPickerDelegate? = null

private class BackupPickerDelegate : NSObject(), UIDocumentPickerDelegateProtocol {
    override fun documentPicker(controller: UIDocumentPickerViewController, didPickDocumentsAtURLs: List<*>) {
        val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL
        val contents = url?.let { selected ->
            readText(selected.path ?: return@let null)
        }
        pendingBackupImport?.invoke(contents)
        pendingBackupImport = null
        activeBackupPickerDelegate = null
    }

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
        pendingBackupImport?.invoke(null)
        pendingBackupImport = null
        activeBackupPickerDelegate = null
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun readText(path: String): String? {
    val file = fopen(path, "rb") ?: return null
    val size = fseek(file, 0, SEEK_END).let { if (it == 0) ftell(file) else -1L }
    if (size <= 0L || size > 25_000_000L) { fclose(file); return null }
    rewind(file)
    val bytes = ByteArray(size.toInt())
    val count = bytes.usePinned { fread(it.addressOf(0), 1u, size.toULong(), file) }
    fclose(file)
    return if (count == size.toULong()) bytes.decodeToString() else null
}

@OptIn(ExperimentalForeignApi::class)
actual fun saveEncryptedBackup(contents: String) {
    val fileName = "penny-backup-${kotlin.time.Clock.System.now().toEpochMilliseconds()}.pennybackup"
    val path = NSTemporaryDirectory() + fileName
    val bytes = contents.encodeToByteArray()
    val saved = memScoped {
        val file = fopen(path, "wb") ?: return@memScoped false
        val written = bytes.usePinned { fwrite(it.addressOf(0), 1u, bytes.size.toULong(), file) }
        fclose(file)
        written == bytes.size.toULong()
    }
    if (!saved) return
    val activityController = UIActivityViewController(listOf(NSURL.fileURLWithPath(path)), null)
    val root = UIApplication.sharedApplication.keyWindow?.rootViewController ?: return
    var presenter = root
    while (presenter.presentedViewController != null) presenter = presenter.presentedViewController!!
    presenter.presentViewController(activityController, animated = true, completion = null)
}

actual fun pickEncryptedBackup(onResult: (String?) -> Unit) {
    pendingBackupImport = onResult
    val pickerDelegate = BackupPickerDelegate()
    activeBackupPickerDelegate = pickerDelegate
    val fileType = UTType.typeWithIdentifier("public.data") ?: run {
        pendingBackupImport = null
        onResult(null)
        return
    }
    val picker = UIDocumentPickerViewController(forOpeningContentTypes = listOf(fileType), asCopy = true)
    picker.delegate = pickerDelegate
    val root = UIApplication.sharedApplication.keyWindow?.rootViewController ?: run {
        pendingBackupImport = null
        onResult(null)
        return
    }
    var presenter = root
    while (presenter.presentedViewController != null) presenter = presenter.presentedViewController!!
    presenter.presentViewController(picker, animated = true, completion = null)
}
