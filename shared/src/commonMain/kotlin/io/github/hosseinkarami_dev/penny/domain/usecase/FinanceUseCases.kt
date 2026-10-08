package io.github.hosseinkarami_dev.penny.domain.usecase

import io.github.hosseinkarami_dev.penny.domain.model.DashboardSummary
import io.github.hosseinkarami_dev.penny.domain.model.BudgetProgress
import io.github.hosseinkarami_dev.penny.domain.model.CategoryBudget
import io.github.hosseinkarami_dev.penny.domain.model.RecurringRule
import io.github.hosseinkarami_dev.penny.domain.model.RecurrenceFrequency
import io.github.hosseinkarami_dev.penny.domain.model.MoneyTransaction
import io.github.hosseinkarami_dev.penny.domain.model.TransactionType
import io.github.hosseinkarami_dev.penny.domain.repository.TransactionRepository
import io.github.hosseinkarami_dev.penny.domain.repository.BudgetRepository
import io.github.hosseinkarami_dev.penny.domain.repository.RecurringRepository
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObserveDashboard(private val repository: TransactionRepository) {
    operator fun invoke(): Flow<DashboardSummary> = repository.observeTransactions().map { items ->
        val income = items.filter { it.type == TransactionType.INCOME }.sumOf { it.amountMinor }
        val expense = items.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amountMinor }
        DashboardSummary(income - expense, income, expense, items.take(5))
    }
}

class ObserveTransactions(private val repository: TransactionRepository) {
    operator fun invoke(): Flow<List<MoneyTransaction>> = repository.observeTransactions()
}

class ObserveBudgetProgress(
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository,
) {
    operator fun invoke(): Flow<List<BudgetProgress>> = combine(
        budgetRepository.observeBudgets(), transactionRepository.observeTransactions(),
    ) { budgets, transactions ->
        budgets.map { budget ->
            val spent = transactions.asSequence()
                .filter { it.type == TransactionType.EXPENSE && it.category.equals(budget.category, ignoreCase = true) }
                .filter { monthKey(it.occurredAtEpochMillis) == budget.monthKey }
                .sumOf { it.amountMinor }
            BudgetProgress(budget, spent)
        }
    }
}

class SaveBudget(private val repository: BudgetRepository) {
    suspend operator fun invoke(budget: CategoryBudget) {
        require(budget.category.isNotBlank()) { "Choose a category for this budget." }
        require(budget.limitMinor > 0) { "Budget limit must be greater than zero." }
        require(budget.monthKey.matches(Regex("\\d{4}-\\d{2}"))) { "Choose a valid budget month." }
        repository.save(budget.copy(category = budget.category.trim()))
    }
}

class DeleteBudget(private val repository: BudgetRepository) {
    suspend operator fun invoke(id: String) = repository.delete(id)
}

class SaveRecurringRule(private val repository: RecurringRepository) {
    suspend operator fun invoke(rule: RecurringRule) {
        require(rule.title.isNotBlank()) { "Add a description for this recurring transaction." }
        require(rule.amountMinor > 0) { "Amount must be greater than zero." }
        require(rule.category.isNotBlank()) { "Choose a category." }
        repository.save(rule.copy(title = rule.title.trim(), category = rule.category.trim()))
    }
}

class ObserveRecurringRules(private val repository: RecurringRepository) {
    operator fun invoke(): Flow<List<RecurringRule>> = repository.observeRules()
}

class DeleteRecurringRule(private val repository: RecurringRepository) {
    suspend operator fun invoke(id: String) = repository.delete(id)
}

class ProcessDueRecurringTransactions(private val repository: RecurringRepository) {
    suspend operator fun invoke(nowEpochMillis: Long) {
        repository.dueBefore(nowEpochMillis).forEach { initial ->
            var rule = initial
            var processed = 0
            while (rule.nextDueAtEpochMillis <= nowEpochMillis && processed < MAX_CATCH_UP_OCCURRENCES) {
                val dueAt = rule.nextDueAtEpochMillis
                val advanced = rule.copy(nextDueAtEpochMillis = nextRecurringOccurrence(rule.frequency, dueAt))
                repository.recordOccurrence(
                    MoneyTransaction("${rule.id}_$dueAt", rule.title, rule.amountMinor, rule.type, rule.category, dueAt),
                    advanced,
                )
                rule = advanced
                processed++
            }
        }
    }

    private companion object {
        const val MAX_CATCH_UP_OCCURRENCES = 120
    }
}

fun nextRecurringOccurrence(frequency: RecurrenceFrequency, epochMillis: Long): Long = when (frequency) {
    RecurrenceFrequency.DAILY -> epochMillis + 86_400_000L
    RecurrenceFrequency.WEEKLY -> epochMillis + 7 * 86_400_000L
    RecurrenceFrequency.MONTHLY -> {
        val zone = TimeZone.currentSystemDefault()
        val local = Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(zone)
        LocalDateTime(local.date.plus(1, DateTimeUnit.MONTH), local.time).toInstant(zone).toEpochMilliseconds()
    }
}

fun monthKey(epochMillis: Long, timeZone: TimeZone = TimeZone.currentSystemDefault()): String {
    val date = Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(timeZone).date
    return "${date.year}-${(date.month.ordinal + 1).toString().padStart(2, '0')}"
}

class SaveTransaction(private val repository: TransactionRepository) {
    suspend operator fun invoke(transaction: MoneyTransaction) {
        require(transaction.title.isNotBlank()) { "Add a description for this transaction." }
        require(transaction.amountMinor > 0) { "Amount must be greater than zero." }
        require(transaction.category.isNotBlank()) { "Choose a category." }
        repository.save(transaction.copy(title = transaction.title.trim(), category = transaction.category.trim()))
    }
}

class DeleteTransaction(private val repository: TransactionRepository) {
    suspend operator fun invoke(id: String) = repository.delete(id)
}
