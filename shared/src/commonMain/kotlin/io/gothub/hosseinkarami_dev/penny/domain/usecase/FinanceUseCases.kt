package io.gothub.hosseinkarami_dev.penny.domain.usecase

import io.gothub.hosseinkarami_dev.penny.domain.model.DashboardSummary
import io.gothub.hosseinkarami_dev.penny.domain.model.MoneyTransaction
import io.gothub.hosseinkarami_dev.penny.domain.model.TransactionType
import io.gothub.hosseinkarami_dev.penny.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObserveDashboard(private val repository: TransactionRepository) {
    operator fun invoke(): Flow<DashboardSummary> = repository.observeTransactions().map { items ->
        val income = items.filter { it.type == TransactionType.INCOME }.sumOf { it.amountMinor }
        val expense = items.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amountMinor }
        DashboardSummary(income - expense, income, expense, items.take(5))
    }
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
