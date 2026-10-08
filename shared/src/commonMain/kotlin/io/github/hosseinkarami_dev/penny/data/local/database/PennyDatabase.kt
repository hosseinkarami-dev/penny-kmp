package io.github.hosseinkarami_dev.penny.data.local.database

import androidx.room.ConstructedBy
import androidx.room.Dao
import androidx.room.Database
import androidx.room.AutoMigration
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

    @Query("SELECT * FROM transactions ORDER BY occurredAtEpochMillis DESC")
    suspend fun getAll(): List<TransactionEntity>

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<TransactionEntity>)
}

@Database(entities = [TransactionEntity::class, BudgetEntity::class, RecurringRuleEntity::class, PreferenceEntity::class], version = 4, exportSchema = true, autoMigrations = [AutoMigration(from = 1, to = 2), AutoMigration(from = 2, to = 3), AutoMigration(from = 3, to = 4)])
@ConstructedBy(PennyDatabaseConstructor::class)
abstract class PennyDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun recurringRuleDao(): RecurringRuleDao
    abstract fun preferenceDao(): PreferenceDao
}

@Suppress("KotlinNoActualForExpect")
expect object PennyDatabaseConstructor : RoomDatabaseConstructor<PennyDatabase>
