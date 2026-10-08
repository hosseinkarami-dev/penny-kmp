package io.github.hosseinkarami_dev.penny

import io.github.hosseinkarami_dev.penny.domain.model.MoneyTransaction
import io.github.hosseinkarami_dev.penny.domain.model.AppearanceMode
import io.github.hosseinkarami_dev.penny.domain.model.DisplayCurrency
import io.github.hosseinkarami_dev.penny.domain.model.TransactionType
import io.github.hosseinkarami_dev.penny.domain.model.CategoryBudget
import io.github.hosseinkarami_dev.penny.domain.model.RecurringRule
import io.github.hosseinkarami_dev.penny.domain.model.RecurrenceFrequency
import io.github.hosseinkarami_dev.penny.domain.model.AppLanguage
import io.github.hosseinkarami_dev.penny.domain.backup.EncryptedBackupCodec
import io.github.hosseinkarami_dev.penny.domain.backup.PennyBackup
import io.github.hosseinkarami_dev.penny.domain.repository.BudgetRepository
import io.github.hosseinkarami_dev.penny.domain.repository.RecurringRepository
import io.github.hosseinkarami_dev.penny.domain.repository.TransactionRepository
import io.github.hosseinkarami_dev.penny.domain.usecase.ObserveBudgetProgress
import io.github.hosseinkarami_dev.penny.domain.usecase.ProcessDueRecurringTransactions
import io.github.hosseinkarami_dev.penny.domain.usecase.ObserveDashboard
import io.github.hosseinkarami_dev.penny.domain.usecase.SaveTransaction
import io.github.hosseinkarami_dev.penny.domain.usecase.ExportTransactionsCsv
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SharedCommonTest {
    @Test
    fun languagePreferenceSupportsAllRequestedLocalesAndArabicPersianRtl() {
        assertEquals(8, AppLanguage.entries.size)
        assertEquals(AppLanguage.ARABIC, AppLanguage.fromStoredValue("ARABIC"))
        assertEquals(true, AppLanguage.ARABIC.isRightToLeft)
        assertEquals(true, AppLanguage.PERSIAN.isRightToLeft)
        assertEquals(false, AppLanguage.SPANISH.isRightToLeft)
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromStoredValue("unsupported"))
        assertEquals("الرئيسية", appTranslation("Home", AppLanguage.ARABIC))
        assertEquals("Inicio", appTranslation("Home", AppLanguage.SPANISH))
        assertEquals("Accueil", appTranslation("Home", AppLanguage.FRENCH))
        assertEquals("首页", appTranslation("Home", AppLanguage.CHINESE))
        assertEquals("ホーム", appTranslation("Home", AppLanguage.JAPANESE))
        assertEquals("홈", appTranslation("Home", AppLanguage.KOREAN))
    }

    @Test
    fun appearancePreferenceFallsBackToSystemAndReadsSavedModes() {
        assertEquals(AppearanceMode.SYSTEM, AppearanceMode.fromStoredValue(null))
        assertEquals(AppearanceMode.SYSTEM, AppearanceMode.fromStoredValue("unknown"))
        assertEquals(AppearanceMode.DARK, AppearanceMode.fromStoredValue("DARK"))
    }

    @Test
    fun currencyPreferenceDefaultsSafelyAndSupportsConfiguredCodes() {
        assertEquals(DisplayCurrency.USD, DisplayCurrency.fromStoredValue(null))
        assertEquals(DisplayCurrency.USD, DisplayCurrency.fromStoredValue("invalid"))
        assertEquals(DisplayCurrency.EUR, DisplayCurrency.fromStoredValue("EUR"))
        assertEquals("CA$", DisplayCurrency.CAD.symbol)
    }

    @Test
    fun csvExportEscapesFieldsAndIncludesCurrencyAndIsoTimestamp() {
        val transaction = transaction("a1", 123_450, TransactionType.EXPENSE, epoch("2026-10-09T12:00:00Z"))
            .copy(title = "Lunch, \"team\"", category = "Food", note = "Cafe\nnear work")

        val csv = ExportTransactionsCsv()(listOf(transaction), "EUR")

        assertEquals(true, csv.contains("\"Lunch, \"\"team\"\"\""))
        assertEquals(true, csv.contains("\"1,234.50\",\"EUR\""))
        assertEquals(true, csv.contains("\"2026-10-09T12:00:00Z\""))
        assertEquals(true, csv.contains("\"Cafe\nnear work\""))
    }

    @Test
    fun encryptedBackupRoundTripsAllLocalDataAndRejectsWrongPassphrase() = runTest {
        val snapshot = PennyBackup(
            transactions = listOf(transaction("coffee", 1_250, TransactionType.EXPENSE, epoch("2026-10-09T12:00:00Z"))),
            budgets = listOf(CategoryBudget("food", "Food", 50_000, "2026-10")),
            recurringRules = listOf(RecurringRule("rent", "Rent", 120_000, TransactionType.EXPENSE, "Housing", RecurrenceFrequency.MONTHLY, 1_800_000_000_000)),
            preferences = mapOf("currency" to "EUR", "app_language" to "PERSIAN"),
        )
        val codec = EncryptedBackupCodec()
        val encrypted = codec.encrypt(snapshot, "correct horse battery")

        assertEquals(snapshot, codec.decrypt(encrypted, "correct horse battery"))
        assertFailsWith<IllegalArgumentException> { codec.decrypt(encrypted, "wrong passphrase") }
        assertFailsWith<IllegalArgumentException> { codec.encrypt(snapshot, "short") }
    }

    @Test
    fun pbkdf2Sha256MatchesPublishedTestVector() {
        val key = EncryptedBackupCodec().deriveKey("password", "salt".encodeToByteArray(), iterations = 1)
        val hex = key.joinToString("") { it.toUByte().toString(16).padStart(2, '0') }

        assertEquals("120fb6cffcf8b32c43e7225256c4f837a86548c92ccc35480805987cb70be17b", hex)
    }

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

    @Test
    fun monthlyBudgetCountsOnlyMatchingCategoryAndMonth() = runTest {
        val month = "2026-10"
        val groceries = CategoryBudget("grocery", "Groceries", 10_000, month)
        val repository = FakeRepository(listOf(
            transaction("current", 7_500, TransactionType.EXPENSE, epoch("2026-10-08T12:00:00Z")).copy(category = "groceries"),
            transaction("income", 20_000, TransactionType.INCOME, epoch("2026-10-09T12:00:00Z")).copy(category = "Groceries"),
            transaction("prior", 9_000, TransactionType.EXPENSE, epoch("2026-09-28T12:00:00Z")).copy(category = "Groceries"),
        ))

        val progress = ObserveBudgetProgress(FakeBudgetRepository(listOf(groceries)), repository).invoke().first().single()

        assertEquals(7_500, progress.spentMinor)
        assertEquals(2_500, progress.remainingMinor)
        assertEquals(false, progress.isOverBudget)
    }

    @Test
    fun recurringRulesCatchUpAndAdvanceWithoutDuplicateOccurrenceIds() = runTest {
        val due = epoch("2026-10-01T12:00:00Z")
        val rule = RecurringRule("rent-rule", "Rent", 120_000, TransactionType.EXPENSE, "Housing", RecurrenceFrequency.WEEKLY, due)
        val repository = FakeRecurringRepository(rule)

        ProcessDueRecurringTransactions(repository)(epoch("2026-10-20T12:00:00Z"))

        assertEquals(3, repository.occurrences.size)
        assertEquals(3, repository.occurrences.map { it.id }.distinct().size)
        assertEquals(epoch("2026-10-22T12:00:00Z"), repository.rule?.nextDueAtEpochMillis)
    }

    private fun transaction(id: String, amount: Long, type: TransactionType, time: Long) =
        MoneyTransaction(id, id, amount, type, "General", time)

    private fun epoch(value: String) = kotlin.time.Instant.parse(value).toEpochMilliseconds()

    private class FakeBudgetRepository(private val budgets: List<CategoryBudget>) : BudgetRepository {
        override fun observeBudgets() = flowOf(budgets)
        override suspend fun save(budget: CategoryBudget) = Unit
        override suspend fun delete(id: String) = Unit
    }

    private class FakeRecurringRepository(initial: RecurringRule) : RecurringRepository {
        var rule: RecurringRule? = initial
        val occurrences = mutableListOf<MoneyTransaction>()
        override fun observeRules() = flowOf(listOfNotNull(rule))
        override suspend fun save(rule: RecurringRule) { this.rule = rule }
        override suspend fun delete(id: String) { if (rule?.id == id) rule = null }
        override suspend fun dueBefore(epochMillis: Long) = listOfNotNull(rule?.takeIf { it.nextDueAtEpochMillis <= epochMillis })
        override suspend fun recordOccurrence(transaction: MoneyTransaction, advancedRule: RecurringRule) {
            if (occurrences.none { it.id == transaction.id }) occurrences += transaction
            rule = advancedRule
        }
    }

    private class FakeRepository(private val initial: List<MoneyTransaction>) : TransactionRepository {
        var saved: MoneyTransaction? = null
        override fun observeTransactions(): Flow<List<MoneyTransaction>> = flowOf(initial.sortedByDescending { it.occurredAtEpochMillis })
        override suspend fun save(transaction: MoneyTransaction) { saved = transaction }
        override suspend fun delete(id: String) = Unit
    }
}
