package io.gothub.hosseinkarami_dev.penny.data.local.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.gothub.hosseinkarami_dev.penny.domain.model.MoneyTransaction
import io.gothub.hosseinkarami_dev.penny.domain.model.TransactionType

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val title: String,
    val amountMinor: Long,
    val type: String,
    val category: String,
    val occurredAtEpochMillis: Long,
    val note: String,
)

fun TransactionEntity.toDomain() = MoneyTransaction(
    id, title, amountMinor, TransactionType.valueOf(type), category, occurredAtEpochMillis, note,
)

fun MoneyTransaction.toEntity() = TransactionEntity(
    id, title, amountMinor, type.name, category, occurredAtEpochMillis, note,
)
