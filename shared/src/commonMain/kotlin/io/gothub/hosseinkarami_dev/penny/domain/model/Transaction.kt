package io.gothub.hosseinkarami_dev.penny.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class TransactionType { INCOME, EXPENSE }

@Serializable
data class MoneyTransaction(
    val id: String,
    val title: String,
    val amountMinor: Long,
    val type: TransactionType,
    val category: String,
    val occurredAtEpochMillis: Long,
    val note: String = "",
)

data class DashboardSummary(
    val balanceMinor: Long,
    val incomeMinor: Long,
    val expenseMinor: Long,
    val recentTransactions: List<MoneyTransaction>,
)
