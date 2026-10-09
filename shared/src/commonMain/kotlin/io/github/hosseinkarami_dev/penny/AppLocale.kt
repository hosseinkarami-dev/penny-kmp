package io.github.hosseinkarami_dev.penny

import androidx.compose.runtime.Composable

@Composable
internal expect fun AppLocaleContent(languageTag: String, content: @Composable () -> Unit)
