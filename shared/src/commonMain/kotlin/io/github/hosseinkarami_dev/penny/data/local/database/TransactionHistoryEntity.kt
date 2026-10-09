package io.github.hosseinkarami_dev.penny.data.local.database

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import io.github.hosseinkarami_dev.penny.domain.model.MoneyTransaction
import io.github.hosseinkarami_dev.penny.domain.model.TransactionRevision
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import kotlin.random.Random

@Entity(tableName = "transaction_history")
data class TransactionHistoryEntity(
    @PrimaryKey val id: String,
    val transactionId: String,
    val action: String,
    val changedAtEpochMillis: Long,
    val beforeJson: String?,
    val afterJson: String?,
)

@Dao
interface TransactionHistoryDao {
    @Query("SELECT * FROM transaction_history WHERE transactionId = :transactionId ORDER BY changedAtEpochMillis DESC")
    fun observeForTransaction(transactionId: String): Flow<List<TransactionHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: TransactionHistoryEntity)

    @Query("DELETE FROM transaction_history")
    suspend fun deleteAll()
}

fun TransactionHistoryEntity.toDomain(json: Json = Json): TransactionRevision = TransactionRevision(
    id = id,
    action = action,
    changedAtEpochMillis = changedAtEpochMillis,
    before = beforeJson?.let { runCatching { json.decodeFromString<MoneyTransaction>(it) }.getOrNull() },
    after = afterJson?.let { runCatching { json.decodeFromString<MoneyTransaction>(it) }.getOrNull() },
)

fun transactionHistoryEntry(
    transactionId: String,
    action: String,
    changedAtEpochMillis: Long,
    before: MoneyTransaction?,
    after: MoneyTransaction?,
    json: Json = Json,
) = TransactionHistoryEntity(
    id = "$transactionId-$changedAtEpochMillis-${Random.nextLong()}",
    transactionId = transactionId,
    action = action,
    changedAtEpochMillis = changedAtEpochMillis,
    beforeJson = before?.let { json.encodeToString(MoneyTransaction.serializer(), it) },
    afterJson = after?.let { json.encodeToString(MoneyTransaction.serializer(), it) },
)
