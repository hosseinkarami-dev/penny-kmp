package io.github.hosseinkarami_dev.penny.data.repository

import io.github.hosseinkarami_dev.penny.data.local.database.BudgetDao
import io.github.hosseinkarami_dev.penny.data.local.database.toDomain
import io.github.hosseinkarami_dev.penny.data.local.database.toEntity
import io.github.hosseinkarami_dev.penny.domain.model.CategoryBudget
import io.github.hosseinkarami_dev.penny.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomBudgetRepository(private val dao: BudgetDao) : BudgetRepository {
    override fun observeBudgets(): Flow<List<CategoryBudget>> = dao.observeAll().map { rows -> rows.map { it.toDomain() } }
    override suspend fun save(budget: CategoryBudget) = dao.upsert(budget.toEntity())
    override suspend fun delete(id: String) = dao.deleteById(id)
}
