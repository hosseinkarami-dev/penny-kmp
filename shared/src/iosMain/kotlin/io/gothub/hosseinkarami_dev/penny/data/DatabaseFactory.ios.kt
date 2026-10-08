package io.gothub.hosseinkarami_dev.penny.data.local.database

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
actual fun createPennyDatabase(): PennyDatabase {
    val directory = NSFileManager.defaultManager.URLForDirectory(
        NSApplicationSupportDirectory,
        NSUserDomainMask,
        null,
        true,
        null,
    ) ?: error("Unable to locate Penny's local data directory.")
    val dbUrl = directory.URLByAppendingPathComponent("penny.db") ?: error("Unable to create database path.")
    return Room.databaseBuilder<PennyDatabase>(name = requireNotNull(dbUrl.path))
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.Default)
        .build()
}
