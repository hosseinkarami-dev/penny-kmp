package io.github.hosseinkarami_dev.penny.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class RecurrenceFrequency { DAILY, WEEKLY, MONTHLY }

@Serializable
data class RecurringRule(
    val id: String,
    val title: String,
    val amountMinor: Long,
    val type: TransactionType,
    val category: String,
    val frequency: RecurrenceFrequency,
    val nextDueAtEpochMillis: Long,
)
