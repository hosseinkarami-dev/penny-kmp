package io.github.hosseinkarami_dev.penny.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class CategoryBudget(
    val id: String,
    val category: String,
    val limitMinor: Long,
    val monthKey: String,
)

data class BudgetProgress(
    val budget: CategoryBudget,
    val spentMinor: Long,
) {
    val remainingMinor: Long get() = (budget.limitMinor - spentMinor).coerceAtLeast(0)
    val progress: Float get() = if (budget.limitMinor <= 0) 0f else (spentMinor.toFloat() / budget.limitMinor).coerceIn(0f, 1f)
    val isOverBudget: Boolean get() = spentMinor > budget.limitMinor
    val isNearLimit: Boolean get() = budget.limitMinor > 0 && spentMinor.toDouble() / budget.limitMinor.toDouble() >= .8
}
