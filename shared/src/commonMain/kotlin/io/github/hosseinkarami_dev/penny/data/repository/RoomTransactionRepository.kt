package io.github.hosseinkarami_dev.penny.data.repository

import io.github.hosseinkarami_dev.penny.data.local.database.TransactionDao
import io.github.hosseinkarami_dev.penny.data.local.database.toDomain
import io.github.hosseinkarami_dev.penny.data.local.database.toEntity
import io.github.hosseinkarami_dev.penny.domain.model.MoneyTransaction
import io.github.hosseinkarami_dev.penny.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomTransactionRepository(private val dao: TransactionDao) : TransactionRepository {
    override fun observeTransactions(): Flow<List<MoneyTransaction>> = dao.observeAll().map { rows -> rows.map { it.toDomain() } }
    override suspend fun save(transaction: MoneyTransaction) = dao.upsert(transaction.toEntity())
    override suspend fun delete(id: String) = dao.deleteById(id)
}
