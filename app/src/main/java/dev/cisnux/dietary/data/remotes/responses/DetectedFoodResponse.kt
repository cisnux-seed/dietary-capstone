package dev.cisnux.dietary.data.remotes.responses

import kotlinx.serialization.Serializable

@Serializable
data class DetectedFoodResponse(
    val id: String,
    val name: String,
    val calorie: Float,
    val fat: Float,
    val protein: Float,
    val carbohydrates: Float,
    // it can be null
    val sugar: Float? = null,
    val questions: List<QuestionResponse>? = null
)
