package io.github.hosseinkarami_dev.penny.domain.model

enum class AppearanceMode {
    SYSTEM, LIGHT, DARK;

    companion object {
        fun fromStoredValue(value: String?): AppearanceMode = entries.firstOrNull { it.name == value } ?: SYSTEM
    }
}
