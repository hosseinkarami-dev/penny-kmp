package io.github.hosseinkarami_dev.penny.domain.model

enum class DisplayCurrency(val displayName: String, val code: String, val symbol: String) {
    USD("US Dollar", "USD", "US$"),
    EUR("Euro", "EUR", "€"),
    GBP("British Pound", "GBP", "£"),
    CAD("Canadian Dollar", "CAD", "CA$"),
    AUD("Australian Dollar", "AUD", "A$");

    companion object {
        fun fromStoredValue(value: String?): DisplayCurrency = entries.firstOrNull { it.code == value } ?: USD
    }
}
