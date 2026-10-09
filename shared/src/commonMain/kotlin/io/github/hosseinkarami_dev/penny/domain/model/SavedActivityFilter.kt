package io.github.hosseinkarami_dev.penny.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class SavedActivityFilter(
    val query: String,
    val type: String? = null,
    val category: String? = null,
    val periodDays: Int? = null,
)
