package io.github.hosseinkarami_dev.penny.domain.model

enum class AppLanguage(val displayName: String, val isRightToLeft: Boolean = false) {
    ENGLISH("English"),
    PERSIAN("فارسی", isRightToLeft = true),
    ARABIC("العربية", isRightToLeft = true),
    SPANISH("Español"),
    FRENCH("Français"),
    CHINESE("中文（简体）"),
    JAPANESE("日本語"),
    KOREAN("한국어");

    companion object {
        fun fromStoredValue(value: String?): AppLanguage = entries.firstOrNull { it.name == value } ?: ENGLISH
    }
}
