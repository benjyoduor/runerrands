package com.example.project.models

enum class UserRole {
    REQUESTOR,
    RUNNER
}

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val role: UserRole = UserRole.REQUESTOR,
    val bio: String = "",
    val rating: Double = 5.0,
    val totalRatings: Int = 0
)
