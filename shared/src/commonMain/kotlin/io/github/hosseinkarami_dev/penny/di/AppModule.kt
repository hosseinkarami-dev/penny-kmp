package io.github.hosseinkarami_dev.penny.di

import io.github.hosseinkarami_dev.penny.data.local.database.PennyDatabase
import io.github.hosseinkarami_dev.penny.data.local.database.createPennyDatabase
import io.github.hosseinkarami_dev.penny.data.repository.RoomTransactionRepository
import io.github.hosseinkarami_dev.penny.data.repository.RoomBudgetRepository
import io.github.hosseinkarami_dev.penny.data.repository.RoomRecurringRepository
import io.github.hosseinkarami_dev.penny.data.repository.RoomPreferencesRepository
import io.github.hosseinkarami_dev.penny.data.repository.RoomBackupRepository
import io.github.hosseinkarami_dev.penny.domain.repository.BudgetRepository
import io.github.hosseinkarami_dev.penny.domain.repository.RecurringRepository
import io.github.hosseinkarami_dev.penny.domain.repository.TransactionRepository
import io.github.hosseinkarami_dev.penny.domain.repository.PreferencesRepository
import io.github.hosseinkarami_dev.penny.domain.backup.BackupRepository
import io.github.hosseinkarami_dev.penny.domain.backup.EncryptedBackupCodec
import io.github.hosseinkarami_dev.penny.domain.backup.CreateEncryptedBackup
import io.github.hosseinkarami_dev.penny.domain.backup.RestoreEncryptedBackup
import io.github.hosseinkarami_dev.penny.domain.usecase.ObserveLanguage
import io.github.hosseinkarami_dev.penny.domain.usecase.SetLanguage
import io.github.hosseinkarami_dev.penny.domain.usecase.ObserveRemindersEnabled
import io.github.hosseinkarami_dev.penny.domain.usecase.SetRemindersEnabled
import io.github.hosseinkarami_dev.penny.domain.usecase.WasBudgetReminderSent
import io.github.hosseinkarami_dev.penny.domain.usecase.MarkBudgetReminderSent
import io.github.hosseinkarami_dev.penny.domain.usecase.ClearBudgetReminderMarkers
import io.github.hosseinkarami_dev.penny.domain.usecase.ObserveAppearanceMode
import io.github.hosseinkarami_dev.penny.domain.usecase.SetAppearanceMode
import io.github.hosseinkarami_dev.penny.domain.usecase.ObserveDisplayCurrency
import io.github.hosseinkarami_dev.penny.domain.usecase.SetDisplayCurrency
import io.github.hosseinkarami_dev.penny.domain.usecase.ObserveCategories
import io.github.hosseinkarami_dev.penny.domain.usecase.SaveCategories
import io.github.hosseinkarami_dev.penny.domain.usecase.ObserveAppLockEnabled
import io.github.hosseinkarami_dev.penny.domain.usecase.SetAppLockEnabled
import io.github.hosseinkarami_dev.penny.domain.usecase.ExportTransactionsCsv
import io.github.hosseinkarami_dev.penny.domain.usecase.DeleteTransaction
import io.github.hosseinkarami_dev.penny.domain.usecase.ObserveDashboard
import io.github.hosseinkarami_dev.penny.domain.usecase.ObserveTransactions
import io.github.hosseinkarami_dev.penny.domain.usecase.ObserveBudgetProgress
import io.github.hosseinkarami_dev.penny.domain.usecase.SaveBudget
import io.github.hosseinkarami_dev.penny.domain.usecase.DeleteBudget
import io.github.hosseinkarami_dev.penny.domain.usecase.SaveRecurringRule
import io.github.hosseinkarami_dev.penny.domain.usecase.DeleteRecurringRule
import io.github.hosseinkarami_dev.penny.domain.usecase.ProcessDueRecurringTransactions
import io.github.hosseinkarami_dev.penny.domain.usecase.ObserveRecurringRules
import io.github.hosseinkarami_dev.penny.domain.usecase.SaveTransaction
import io.github.hosseinkarami_dev.penny.presentation.DashboardViewModel
import org.koin.dsl.module

val appModule = module {
    single { createPennyDatabase() }
    single { get<PennyDatabase>().transactionDao() }
    single { get<PennyDatabase>().budgetDao() }
    single { get<PennyDatabase>().preferenceDao() }
    single<TransactionRepository> { RoomTransactionRepository(get()) }
    single<BudgetRepository> { RoomBudgetRepository(get()) }
    single<RecurringRepository> { RoomRecurringRepository(get()) }
    single<PreferencesRepository> { RoomPreferencesRepository(get()) }
    single<BackupRepository> { RoomBackupRepository(get()) }
    single { EncryptedBackupCodec() }
    factory { CreateEncryptedBackup(get(), get()) }
    factory { RestoreEncryptedBackup(get(), get()) }
    factory { ObserveLanguage(get()) }
    factory { SetLanguage(get()) }
    factory { ObserveRemindersEnabled(get()) }
    factory { SetRemindersEnabled(get()) }
    factory { WasBudgetReminderSent(get()) }
    factory { MarkBudgetReminderSent(get()) }
    factory { ClearBudgetReminderMarkers(get()) }
    factory { ObserveAppearanceMode(get()) }
    factory { SetAppearanceMode(get()) }
    factory { ObserveDisplayCurrency(get()) }
    factory { SetDisplayCurrency(get()) }
    factory { ObserveCategories(get()) }
    factory { SaveCategories(get()) }
    factory { ObserveAppLockEnabled(get()) }
    factory { SetAppLockEnabled(get()) }
    factory { ExportTransactionsCsv() }
    factory { ObserveDashboard(get()) }
    factory { ObserveTransactions(get()) }
    factory { ObserveBudgetProgress(get(), get()) }
    factory { SaveTransaction(get()) }
    factory { SaveBudget(get()) }
    factory { DeleteBudget(get()) }
    factory { SaveRecurringRule(get()) }
    factory { DeleteRecurringRule(get()) }
    factory { ProcessDueRecurringTransactions(get()) }
    factory { ObserveRecurringRules(get()) }
    factory { DeleteTransaction(get()) }
    single { DashboardViewModel(get(), get(), get(), get(), get()) }
}
