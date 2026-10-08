package io.gothub.hosseinkarami_dev.penny.domain.repository

import io.gothub.hosseinkarami_dev.penny.domain.model.MoneyTransaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun observeTransactions(): Flow<List<MoneyTransaction>>
    suspend fun save(transaction: MoneyTransaction)
    suspend fun delete(id: String)
}
