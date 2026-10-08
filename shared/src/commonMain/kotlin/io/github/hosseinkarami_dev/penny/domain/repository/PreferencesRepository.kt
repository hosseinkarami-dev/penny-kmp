package io.github.hosseinkarami_dev.penny.domain.repository

import io.github.hosseinkarami_dev.penny.domain.model.AppearanceMode
import io.github.hosseinkarami_dev.penny.domain.model.DisplayCurrency
import io.github.hosseinkarami_dev.penny.domain.model.FinanceCategory
import io.github.hosseinkarami_dev.penny.domain.model.AppLanguage
import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    fun observeAppearanceMode(): Flow<AppearanceMode>
    suspend fun setAppearanceMode(mode: AppearanceMode)
    fun observeDisplayCurrency(): Flow<DisplayCurrency>
    suspend fun setDisplayCurrency(currency: DisplayCurrency)
    fun observeCategories(): Flow<List<FinanceCategory>>
    suspend fun setCategories(categories: List<FinanceCategory>)
    fun observeAppLockEnabled(): Flow<Boolean>
    suspend fun setAppLockEnabled(enabled: Boolean)
    fun observeLanguage(): Flow<AppLanguage>
    suspend fun setLanguage(language: AppLanguage)
    fun observeRemindersEnabled(): Flow<Boolean>
    suspend fun setRemindersEnabled(enabled: Boolean)
    suspend fun wasBudgetReminderSent(budgetId: String): Boolean = false
    suspend fun markBudgetReminderSent(budgetId: String) = Unit
    suspend fun clearBudgetReminderMarkers() = Unit
}
