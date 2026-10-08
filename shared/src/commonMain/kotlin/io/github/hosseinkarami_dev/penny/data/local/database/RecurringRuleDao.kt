package io.github.hosseinkarami_dev.penny.data.local.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
abstract class RecurringRuleDao {
    @Query("SELECT * FROM recurring_rules ORDER BY nextDueAtEpochMillis")
    abstract fun observeAll(): Flow<List<RecurringRuleEntity>>

    @Query("SELECT * FROM recurring_rules WHERE nextDueAtEpochMillis <= :now ORDER BY nextDueAtEpochMillis")
    abstract suspend fun dueBefore(now: Long): List<RecurringRuleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsert(rule: RecurringRuleEntity)

    @Query("DELETE FROM recurring_rules WHERE id = :id")
    abstract suspend fun deleteById(id: String)

    @Query("SELECT * FROM recurring_rules ORDER BY nextDueAtEpochMillis")
    abstract suspend fun getAll(): List<RecurringRuleEntity>

    @Query("DELETE FROM recurring_rules")
    abstract suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAll(rules: List<RecurringRuleEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insertOccurrence(transaction: TransactionEntity)

    @Transaction
    open suspend fun recordOccurrenceAndAdvance(transaction: TransactionEntity, advancedRule: RecurringRuleEntity) {
        insertOccurrence(transaction)
        upsert(advancedRule)
    }
}
