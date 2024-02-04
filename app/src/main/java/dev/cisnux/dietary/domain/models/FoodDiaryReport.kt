package dev.cisnux.dietary.domain.models

data class FoodDiaryReport(
    val id: String,
    val title: String,
    val totalFoodCalories: Float,
    val label: String
)