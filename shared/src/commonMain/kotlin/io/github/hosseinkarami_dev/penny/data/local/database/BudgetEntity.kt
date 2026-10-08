package io.github.hosseinkarami_dev.penny.data.local.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.github.hosseinkarami_dev.penny.domain.model.CategoryBudget

@Entity(tableName = "category_budgets")
data class BudgetEntity(
    @PrimaryKey val id: String,
    val category: String,
    val limitMinor: Long,
    val monthKey: String,
)

fun BudgetEntity.toDomain() = CategoryBudget(id, category, limitMinor, monthKey)
fun CategoryBudget.toEntity() = BudgetEntity(id, category, limitMinor, monthKey)
