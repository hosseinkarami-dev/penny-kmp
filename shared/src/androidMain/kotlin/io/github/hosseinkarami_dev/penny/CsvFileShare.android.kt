package io.github.hosseinkarami_dev.penny

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File
import android.net.Uri

private lateinit var csvExportContext: Context
private var pendingCsvContents: String? = null
private var launchCsvDocument: ((String) -> Unit)? = null

fun initializeCsvFileShare(context: Context, launchDocument: (String) -> Unit) {
    csvExportContext = context.applicationContext
    launchCsvDocument = launchDocument
}

fun completeCsvSave(uri: Uri?) {
    val contents = pendingCsvContents
    pendingCsvContents = null
    if (uri != null && contents != null) {
        runCatching { csvExportContext.contentResolver.openOutputStream(uri)?.bufferedWriter(Charsets.UTF_8)?.use { it.write(contents) } }
    }
}

actual fun createAndShareCsvFile(contents: String) {
    check(::csvExportContext.isInitialized) { "Initialize CSV export before opening Penny." }
    val context = csvExportContext
    val documents = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: context.filesDir
    val exportDirectory = File(documents, "exports").apply { mkdirs() }
    val file = File(exportDirectory, "penny-transactions-${System.currentTimeMillis()}.csv")
    file.writeText(contents, Charsets.UTF_8)
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(Intent.EXTRA_STREAM, uri)
        clipData = ClipData.newUri(context.contentResolver, file.name, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(shareIntent, "Export transactions").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}

actual fun saveCsvFile(contents: String) {
    check(launchCsvDocument != null) { "Initialize CSV export before opening Penny." }
    pendingCsvContents = contents
    launchCsvDocument?.invoke("penny-transactions.csv")
}
