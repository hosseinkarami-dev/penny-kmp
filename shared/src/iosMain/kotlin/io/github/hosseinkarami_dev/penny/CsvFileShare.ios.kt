package io.github.hosseinkarami_dev.penny

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.usePinned
import platform.Foundation.NSURL
import platform.Foundation.NSTemporaryDirectory
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.posix.fclose
import platform.posix.fopen
import platform.posix.fwrite

@OptIn(ExperimentalForeignApi::class)
actual fun createAndShareCsvFile(contents: String) {
    presentCsvShare(contents)
}

actual fun saveCsvFile(contents: String) {
    // The iOS share sheet includes Save to Files, AirDrop, and installed file providers.
    presentCsvShare(contents)
}

@OptIn(ExperimentalForeignApi::class)
private fun presentCsvShare(contents: String) {
    val fileName = "penny-transactions-${kotlin.time.Clock.System.now().toEpochMilliseconds()}.csv"
    val path = NSTemporaryDirectory() + fileName
    val bytes = contents.encodeToByteArray()
    val saved = memScoped {
        val file = fopen(path, "wb") ?: return@memScoped false
        val written = bytes.usePinned { fwrite(it.addressOf(0), 1u, bytes.size.toULong(), file) }
        fclose(file)
        written == bytes.size.toULong()
    }
    if (!saved) return
    val activityController = UIActivityViewController(
        activityItems = listOf(NSURL.fileURLWithPath(path)),
        applicationActivities = null,
    )
    val root = UIApplication.sharedApplication.keyWindow?.rootViewController ?: return
    var presenter = root
    while (presenter.presentedViewController != null) presenter = presenter.presentedViewController!!
    presenter.presentViewController(activityController, animated = true, completion = null)
}
