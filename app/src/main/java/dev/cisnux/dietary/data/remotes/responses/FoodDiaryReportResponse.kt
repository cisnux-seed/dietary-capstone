package dev.cisnux.dietary.data.remotes.responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FoodDiaryReportResponse(
    val id: String,
    val title: String,
    @SerialName(value = "added_at")
    val addedAt: Long,
    @SerialName(value = "total_food_calories")
    val totalFoodCalories: Float,
)