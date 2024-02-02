package dev.cisnux.dietary.domain.models

import dev.cisnux.dietary.utils.QuestionType

data class Question(
    val id: String,
    val label: String,
    val question: String,
    val type: QuestionType,
    val unit: String?
)