package io.github.hosseinkarami_dev.penny

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import platform.Foundation.NSUserDefaults

@Composable
internal actual fun AppLocaleContent(languageTag: String, content: @Composable () -> Unit) {
    remember(languageTag) {
        NSUserDefaults.standardUserDefaults.setObject(listOf(languageTag), forKey = "AppleLanguages")
        Unit
    }
    key(languageTag) { content() }
}
