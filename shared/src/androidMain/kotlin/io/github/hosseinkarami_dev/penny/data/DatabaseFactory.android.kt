package io.github.hosseinkarami_dev.penny.data.local.database

import android.content.Context
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers

private lateinit var appContext: Context

fun initializeDatabase(context: Context) {
    appContext = context.applicationContext
}

actual fun createPennyDatabase(): PennyDatabase {
    check(::appContext.isInitialized) { "Initialize the local database before starting Penny." }
    val dbFile = appContext.getDatabasePath("penny.db")
    return Room.databaseBuilder<PennyDatabase>(appContext, dbFile.absolutePath)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}
