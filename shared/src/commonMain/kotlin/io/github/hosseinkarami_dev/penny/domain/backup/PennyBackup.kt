package io.github.hosseinkarami_dev.penny.domain.backup

import io.github.hosseinkarami_dev.penny.domain.model.CategoryBudget
import io.github.hosseinkarami_dev.penny.domain.model.MoneyTransaction
import io.github.hosseinkarami_dev.penny.domain.model.RecurringRule
import kotlinx.serialization.Serializable

@Serializable
data class PennyBackup(
    val formatVersion: Int = CURRENT_VERSION,
    val transactions: List<MoneyTransaction>,
    val budgets: List<CategoryBudget>,
    val recurringRules: List<RecurringRule>,
    val preferences: Map<String, String>,
) {
    companion object { const val CURRENT_VERSION = 1 }
}

interface BackupRepository {
    suspend fun createSnapshot(): PennyBackup
    suspend fun restore(snapshot: PennyBackup)
}
