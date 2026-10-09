package io.github.hosseinkarami_dev.penny.domain.model

data class TransactionRevision(
    val id: String,
    val action: String,
    val changedAtEpochMillis: Long,
    val before: MoneyTransaction?,
    val after: MoneyTransaction?,
)
