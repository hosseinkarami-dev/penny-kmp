package io.github.hosseinkarami_dev.penny.data.repository

import io.github.hosseinkarami_dev.penny.data.local.database.PreferenceDao
import io.github.hosseinkarami_dev.penny.data.local.database.PreferenceEntity
import io.github.hosseinkarami_dev.penny.domain.model.AppearanceMode
import io.github.hosseinkarami_dev.penny.domain.model.DisplayCurrency
import io.github.hosseinkarami_dev.penny.domain.model.FinanceCategory
import io.github.hosseinkarami_dev.penny.domain.model.AppLanguage
import io.github.hosseinkarami_dev.penny.domain.model.starterCategories
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import io.github.hosseinkarami_dev.penny.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomPreferencesRepository(private val dao: PreferenceDao) : PreferencesRepository {
    override fun observeAppearanceMode(): Flow<AppearanceMode> = dao.observeValue(APPEARANCE_KEY).map(AppearanceMode::fromStoredValue)
    override suspend fun setAppearanceMode(mode: AppearanceMode) = dao.set(PreferenceEntity(APPEARANCE_KEY, mode.name))
    override fun observeDisplayCurrency(): Flow<DisplayCurrency> = dao.observeValue(CURRENCY_KEY).map(DisplayCurrency::fromStoredValue)
    override suspend fun setDisplayCurrency(currency: DisplayCurrency) = dao.set(PreferenceEntity(CURRENCY_KEY, currency.code))
    override fun observeCategories(): Flow<List<FinanceCategory>> = dao.observeValue(CATEGORIES_KEY).map { stored ->
        stored?.let { runCatching { Json.decodeFromString(ListSerializer(FinanceCategory.serializer()), it) }.getOrNull() }
            ?.takeIf { it.isNotEmpty() } ?: starterCategories
    }
    override suspend fun setCategories(categories: List<FinanceCategory>) {
        val normalized = categories.map { it.copy(name = it.name.trim()) }.filter { it.name.isNotBlank() }.distinctBy { it.name.lowercase() }
        dao.set(PreferenceEntity(CATEGORIES_KEY, Json.encodeToString(ListSerializer(FinanceCategory.serializer()), normalized)))
    }
    override fun observeAppLockEnabled(): Flow<Boolean> = dao.observeValue(APP_LOCK_KEY).map { it == "true" }
    override suspend fun setAppLockEnabled(enabled: Boolean) = dao.set(PreferenceEntity(APP_LOCK_KEY, enabled.toString()))
    override fun observeLanguage(): Flow<AppLanguage> = dao.observeValue(LANGUAGE_KEY).map(AppLanguage::fromStoredValue)
    override suspend fun setLanguage(language: AppLanguage) = dao.set(PreferenceEntity(LANGUAGE_KEY, language.name))
    override fun observeRemindersEnabled(): Flow<Boolean> = dao.observeValue(REMINDERS_KEY).map { it == "true" }
    override suspend fun setRemindersEnabled(enabled: Boolean) = dao.set(PreferenceEntity(REMINDERS_KEY, enabled.toString()))
    override suspend fun wasBudgetReminderSent(budgetId: String): Boolean = dao.getValue(budgetReminderKey(budgetId)) == "true"
    override suspend fun markBudgetReminderSent(budgetId: String) = dao.set(PreferenceEntity(budgetReminderKey(budgetId), "true"))
    override suspend fun clearBudgetReminderMarkers() = dao.deleteByKeyPattern("$BUDGET_REMINDER_PREFIX%")

    private fun budgetReminderKey(id: String) = "$BUDGET_REMINDER_PREFIX$id"

    private companion object {
        const val APPEARANCE_KEY = "appearance_mode"
        const val CURRENCY_KEY = "display_currency"
        const val CATEGORIES_KEY = "finance_categories"
        const val APP_LOCK_KEY = "app_lock_enabled"
        const val LANGUAGE_KEY = "app_language"
        const val REMINDERS_KEY = "local_reminders_enabled"
        const val BUDGET_REMINDER_PREFIX = "budget_reminder_sent_"
    }
}
