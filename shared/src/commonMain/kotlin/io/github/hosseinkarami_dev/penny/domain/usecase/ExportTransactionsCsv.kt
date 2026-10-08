package io.github.hosseinkarami_dev.penny.domain.usecase

import io.github.hosseinkarami_dev.penny.domain.model.MoneyTransaction
import kotlin.time.Instant

class ExportTransactionsCsv {
    operator fun invoke(transactions: List<MoneyTransaction>, currencyCode: String): String = buildString {
        appendLine(listOf("id", "title", "type", "category", "amount", "currency", "occurred_at", "note").joinToString(",") { it.csvCell() })
        transactions.sortedBy { it.occurredAtEpochMillis }.forEach { transaction ->
            val amount = transaction.amountMinor
            val row = listOf(
                transaction.id,
                transaction.title,
                transaction.type.name.lowercase(),
                transaction.category,
                amount.toCsvNumber(),
                currencyCode,
                Instant.fromEpochMilliseconds(transaction.occurredAtEpochMillis).toString(),
                transaction.note,
            )
            appendLine(row.joinToString(",") { it.csvCell() })
        }
    }

    private fun String.csvCell(): String = "\"${replace("\"", "\"\"")}\""

    private fun Long.toCsvNumber(): String {
        val sign = if (this < 0) "-" else ""
        val absolute = kotlin.math.abs(this)
        val whole = (absolute / 100).toString().reversed().chunked(3).joinToString(",").reversed()
        val fraction = (absolute % 100).toString().padStart(2, '0')
        return "$sign$whole.$fraction"
    }
}
