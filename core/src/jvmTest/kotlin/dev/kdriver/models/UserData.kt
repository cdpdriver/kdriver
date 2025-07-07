package dev.kdriver.models

import kotlinx.serialization.Serializable

@Serializable
data class UserData(
    val name: String,
    val title: String,
    val email: String,
    val location: String,
    val avatar: String,
    val bio: String,
    val skills: List<String>,
)
