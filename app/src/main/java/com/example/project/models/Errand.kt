package com.example.project.models

import java.util.UUID

data class Errand(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val description: String = "",
    val budget: Double = 0.0,
    val location: String = "",
    val category: ErrandCategory = ErrandCategory.OTHER,
    val requestorId: String = "",
    val runnerId: String? = null,
    val status: ErrandStatus = ErrandStatus.OPEN,
    val applicants: List<String> = emptyList(),
    val runnerRating: Int? = null,
    val runnerReview: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

enum class ErrandStatus {
    OPEN,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}

enum class ErrandCategory(val icon: String, val displayName: String) {
    DELIVERY("📦", "Delivery"),
    CLEANING("🧹", "Cleaning"),
    SHOPPING("🛒", "Shopping"),
    ASSEMBLY("🔧", "Assembly"),
    MOVING("🚚", "Moving"),
    OTHER("✨", "Other")
}
