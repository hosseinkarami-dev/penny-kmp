package io.github.hosseinkarami_dev.penny.domain.usecase

import io.github.hosseinkarami_dev.penny.domain.model.AppearanceMode
import io.github.hosseinkarami_dev.penny.domain.model.DisplayCurrency
import io.github.hosseinkarami_dev.penny.domain.model.FinanceCategory
import io.github.hosseinkarami_dev.penny.domain.model.AppLanguage
import io.github.hosseinkarami_dev.penny.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow

class ObserveAppearanceMode(private val repository: PreferencesRepository) {
    operator fun invoke(): Flow<AppearanceMode> = repository.observeAppearanceMode()
}

class SetAppearanceMode(private val repository: PreferencesRepository) {
    suspend operator fun invoke(mode: AppearanceMode) = repository.setAppearanceMode(mode)
}

class ObserveDisplayCurrency(private val repository: PreferencesRepository) {
    operator fun invoke(): Flow<DisplayCurrency> = repository.observeDisplayCurrency()
}

class SetDisplayCurrency(private val repository: PreferencesRepository) {
    suspend operator fun invoke(currency: DisplayCurrency) = repository.setDisplayCurrency(currency)
}

class ObserveCategories(private val repository: PreferencesRepository) {
    operator fun invoke(): Flow<List<FinanceCategory>> = repository.observeCategories()
}

class SaveCategories(private val repository: PreferencesRepository) {
    suspend operator fun invoke(categories: List<FinanceCategory>) = repository.setCategories(categories)
}

class ObserveAppLockEnabled(private val repository: PreferencesRepository) {
    operator fun invoke(): Flow<Boolean> = repository.observeAppLockEnabled()
}

class SetAppLockEnabled(private val repository: PreferencesRepository) {
    suspend operator fun invoke(enabled: Boolean) = repository.setAppLockEnabled(enabled)
}

class ObserveLanguage(private val repository: PreferencesRepository) {
    operator fun invoke(): Flow<AppLanguage> = repository.observeLanguage()
}

class SetLanguage(private val repository: PreferencesRepository) {
    suspend operator fun invoke(language: AppLanguage) = repository.setLanguage(language)
}

class ObserveRemindersEnabled(private val repository: PreferencesRepository) {
    operator fun invoke(): Flow<Boolean> = repository.observeRemindersEnabled()
}

class SetRemindersEnabled(private val repository: PreferencesRepository) {
    suspend operator fun invoke(enabled: Boolean) = repository.setRemindersEnabled(enabled)
}

class WasBudgetReminderSent(private val repository: PreferencesRepository) {
    suspend operator fun invoke(budgetId: String): Boolean = repository.wasBudgetReminderSent(budgetId)
}

class MarkBudgetReminderSent(private val repository: PreferencesRepository) {
    suspend operator fun invoke(budgetId: String) = repository.markBudgetReminderSent(budgetId)
}

class ClearBudgetReminderMarkers(private val repository: PreferencesRepository) {
    suspend operator fun invoke() = repository.clearBudgetReminderMarkers()
}
