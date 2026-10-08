package io.github.hosseinkarami_dev.penny.domain.repository

import io.github.hosseinkarami_dev.penny.domain.model.CategoryBudget
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {
    fun observeBudgets(): Flow<List<CategoryBudget>>
    suspend fun save(budget: CategoryBudget)
    suspend fun delete(id: String)
}
