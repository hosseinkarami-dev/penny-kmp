package io.github.hosseinkarami_dev.penny.data.repository

import io.github.hosseinkarami_dev.penny.data.local.database.PennyDatabase
import io.github.hosseinkarami_dev.penny.data.local.database.toDomain
import io.github.hosseinkarami_dev.penny.data.local.database.toEntity
import io.github.hosseinkarami_dev.penny.domain.model.MoneyTransaction
import io.github.hosseinkarami_dev.penny.domain.model.RecurringRule
import io.github.hosseinkarami_dev.penny.domain.repository.RecurringRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomRecurringRepository(private val database: PennyDatabase) : RecurringRepository {
    private val rules get() = database.recurringRuleDao()

    override fun observeRules(): Flow<List<RecurringRule>> = rules.observeAll().map { rows -> rows.map { it.toDomain() } }
    override suspend fun save(rule: RecurringRule) = rules.upsert(rule.toEntity())
    override suspend fun delete(id: String) = rules.deleteById(id)
    override suspend fun dueBefore(epochMillis: Long): List<RecurringRule> = rules.dueBefore(epochMillis).map { it.toDomain() }
    override suspend fun recordOccurrence(transaction: MoneyTransaction, advancedRule: RecurringRule) {
        rules.recordOccurrenceAndAdvance(transaction.toEntity(), advancedRule.toEntity())
    }
}
