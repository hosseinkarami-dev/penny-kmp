package io.github.hosseinkarami_dev.penny.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class FinanceCategory(val name: String)

val starterCategories = listOf(
    "Food & Dining", "Groceries", "Transport", "Housing", "Bills", "Shopping",
    "Health", "Entertainment", "Travel", "Education", "Salary", "Gifts", "Other",
).map(::FinanceCategory)
