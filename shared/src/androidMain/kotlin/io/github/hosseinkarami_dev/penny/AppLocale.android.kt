package io.github.hosseinkarami_dev.penny

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import java.util.Locale

@Composable
internal actual fun AppLocaleContent(languageTag: String, content: @Composable () -> Unit) {
    val current = LocalConfiguration.current
    val localized = remember(current, languageTag) {
        Configuration(current).apply {
            val locale = Locale.forLanguageTag(languageTag)
            setLocale(locale)
            setLayoutDirection(locale)
        }
    }
    CompositionLocalProvider(LocalConfiguration provides localized, content = content)
}
