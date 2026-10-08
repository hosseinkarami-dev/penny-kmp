package io.gothub.hosseinkarami_dev.penny

import io.gothub.hosseinkarami_dev.penny.domain.model.MoneyTransaction
import io.gothub.hosseinkarami_dev.penny.domain.model.TransactionType
import io.gothub.hosseinkarami_dev.penny.domain.repository.TransactionRepository
import io.gothub.hosseinkarami_dev.penny.domain.usecase.ObserveDashboard
import io.gothub.hosseinkarami_dev.penny.domain.usecase.SaveTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SharedCommonTest {
    @Test
    fun dashboardTotalsUseMinorUnitsAndShowNewestTransactions() = runTest {
        val entries = listOf(
            transaction("salary", 250_000, TransactionType.INCOME, 1),
            transaction("rent", 90_000, TransactionType.EXPENSE, 2),
            transaction("coffee", 500, TransactionType.EXPENSE, 3),
        )
        val summary = ObserveDashboard(FakeRepository(entries)).invoke().first()

        assertEquals(159_500, summary.balanceMinor)
        assertEquals(250_000, summary.incomeMinor)
        assertEquals(90_500, summary.expenseMinor)
        assertEquals(listOf("coffee", "rent", "salary"), summary.recentTransactions.map { it.id })
    }

    @Test
    fun saveRejectsZeroOrNegativeAmounts() = runTest {
        val repository = FakeRepository(emptyList())
        assertFailsWith<IllegalArgumentException> {
            SaveTransaction(repository)(transaction("invalid", 0, TransactionType.EXPENSE, 1))
        }
        assertEquals(null, repository.saved)
    }

    @Test
    fun saveTrimsUserEnteredTextBeforePersisting() = runTest {
        val repository = FakeRepository(emptyList())
        SaveTransaction(repository)(transaction("new", 1_250, TransactionType.EXPENSE, 1).copy(title = "  Lunch ", category = " Food "))
        assertEquals("Lunch", repository.saved?.title)
        assertEquals("Food", repository.saved?.category)
    }

    private fun transaction(id: String, amount: Long, type: TransactionType, time: Long) =
        MoneyTransaction(id, id, amount, type, "General", time)

    private class FakeRepository(private val initial: List<MoneyTransaction>) : TransactionRepository {
        var saved: MoneyTransaction? = null
        override fun observeTransactions(): Flow<List<MoneyTransaction>> = flowOf(initial.sortedByDescending { it.occurredAtEpochMillis })
        override suspend fun save(transaction: MoneyTransaction) { saved = transaction }
        override suspend fun delete(id: String) = Unit
    }
}
