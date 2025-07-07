package dev.kdriver.models

import kotlinx.serialization.Serializable

@Serializable
data class TodoItem(
    val id: Long,
    val userId: Long,
    val title: String,
    val completed: Boolean,
)
