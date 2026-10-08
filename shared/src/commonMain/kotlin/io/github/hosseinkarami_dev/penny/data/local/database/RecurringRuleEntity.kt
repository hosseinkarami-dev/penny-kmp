package io.github.hosseinkarami_dev.penny.data.local.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.github.hosseinkarami_dev.penny.domain.model.RecurringRule
import io.github.hosseinkarami_dev.penny.domain.model.RecurrenceFrequency
import io.github.hosseinkarami_dev.penny.domain.model.TransactionType

@Entity(tableName = "recurring_rules")
data class RecurringRuleEntity(
    @PrimaryKey val id: String,
    val title: String,
    val amountMinor: Long,
    val type: String,
    val category: String,
    val frequency: String,
    val nextDueAtEpochMillis: Long,
)

fun RecurringRuleEntity.toDomain() = RecurringRule(id, title, amountMinor, TransactionType.valueOf(type), category, RecurrenceFrequency.valueOf(frequency), nextDueAtEpochMillis)
fun RecurringRule.toEntity() = RecurringRuleEntity(id, title, amountMinor, type.name, category, frequency.name, nextDueAtEpochMillis)
