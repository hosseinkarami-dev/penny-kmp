package io.github.hosseinkarami_dev.penny.data.repository

import androidx.room.useWriterConnection
import androidx.room.immediateTransaction
import io.github.hosseinkarami_dev.penny.data.local.database.PennyDatabase
import io.github.hosseinkarami_dev.penny.data.local.database.PreferenceEntity
import io.github.hosseinkarami_dev.penny.data.local.database.toDomain
import io.github.hosseinkarami_dev.penny.data.local.database.toEntity
import io.github.hosseinkarami_dev.penny.domain.backup.BackupRepository
import io.github.hosseinkarami_dev.penny.domain.backup.PennyBackup

class RoomBackupRepository(private val database: PennyDatabase) : BackupRepository {
    override suspend fun createSnapshot(): PennyBackup = PennyBackup(
        transactions = database.transactionDao().getAll().map { it.toDomain() },
        budgets = database.budgetDao().getAll().map { it.toDomain() },
        recurringRules = database.recurringRuleDao().getAll().map { it.toDomain() },
        preferences = database.preferenceDao().getAll().associate { it.key to it.value },
    )

    override suspend fun restore(snapshot: PennyBackup) {
        require(snapshot.formatVersion == PennyBackup.CURRENT_VERSION) { "This backup version is not supported." }
        require(snapshot.transactions.map { it.id }.distinct().size == snapshot.transactions.size) { "The backup contains duplicate transactions." }
        require(snapshot.budgets.map { it.id }.distinct().size == snapshot.budgets.size) { "The backup contains duplicate budgets." }
        require(snapshot.recurringRules.map { it.id }.distinct().size == snapshot.recurringRules.size) { "The backup contains duplicate recurring rules." }
        require(snapshot.transactions.all { it.title.isNotBlank() && it.amountMinor > 0 && it.category.isNotBlank() }) { "The backup contains invalid transactions." }
        require(snapshot.budgets.all { it.category.isNotBlank() && it.limitMinor > 0 }) { "The backup contains invalid budgets." }
        require(snapshot.recurringRules.all { it.title.isNotBlank() && it.amountMinor > 0 && it.category.isNotBlank() }) { "The backup contains invalid recurring rules." }

        database.useWriterConnection { transactor ->
            transactor.immediateTransaction {
                database.transactionDao().deleteAll()
                database.budgetDao().deleteAll()
                database.recurringRuleDao().deleteAll()
                database.preferenceDao().deleteAll()
                database.transactionDao().insertAll(snapshot.transactions.map { it.toEntity() })
                database.budgetDao().insertAll(snapshot.budgets.map { it.toEntity() })
                database.recurringRuleDao().insertAll(snapshot.recurringRules.map { it.toEntity() })
                database.preferenceDao().setAll(snapshot.preferences.map { (key, value) -> PreferenceEntity(key, value) })
            }
        }
    }
}
