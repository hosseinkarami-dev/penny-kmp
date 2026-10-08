package io.gothub.hosseinkarami_dev.penny.di

import io.gothub.hosseinkarami_dev.penny.data.local.database.PennyDatabase
import io.gothub.hosseinkarami_dev.penny.data.local.database.createPennyDatabase
import io.gothub.hosseinkarami_dev.penny.data.repository.RoomTransactionRepository
import io.gothub.hosseinkarami_dev.penny.domain.repository.TransactionRepository
import io.gothub.hosseinkarami_dev.penny.domain.usecase.DeleteTransaction
import io.gothub.hosseinkarami_dev.penny.domain.usecase.ObserveDashboard
import io.gothub.hosseinkarami_dev.penny.domain.usecase.SaveTransaction
import io.gothub.hosseinkarami_dev.penny.presentation.DashboardViewModel
import org.koin.dsl.module

val appModule = module {
    single { createPennyDatabase() }
    single { get<PennyDatabase>().transactionDao() }
    single<TransactionRepository> { RoomTransactionRepository(get()) }
    factory { ObserveDashboard(get()) }
    factory { SaveTransaction(get()) }
    factory { DeleteTransaction(get()) }
    single { DashboardViewModel(get()) }
}
