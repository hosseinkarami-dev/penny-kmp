package io.github.hosseinkarami_dev.penny.domain.repository

import io.github.hosseinkarami_dev.penny.domain.model.MoneyTransaction
import io.github.hosseinkarami_dev.penny.domain.model.RecurringRule
import kotlinx.coroutines.flow.Flow

interface RecurringRepository {
    fun observeRules(): Flow<List<RecurringRule>>
    suspend fun save(rule: RecurringRule)
    suspend fun delete(id: String)
    suspend fun dueBefore(epochMillis: Long): List<RecurringRule>
    suspend fun recordOccurrence(transaction: MoneyTransaction, advancedRule: RecurringRule)
}
