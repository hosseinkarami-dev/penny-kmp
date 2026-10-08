package io.gothub.hosseinkarami_dev.penny.data.local.database

import androidx.room.ConstructedBy
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY occurredAtEpochMillis DESC")
    fun observeAll(): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Database(entities = [TransactionEntity::class], version = 1, exportSchema = true)
@ConstructedBy(PennyDatabaseConstructor::class)
abstract class PennyDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
}

@Suppress("KotlinNoActualForExpect")
expect object PennyDatabaseConstructor : RoomDatabaseConstructor<PennyDatabase>
