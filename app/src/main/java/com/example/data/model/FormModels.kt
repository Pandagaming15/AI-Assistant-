package com.example.data.model

data class FormOption(
    val index: Int,
    val label: String, // "A", "B", "1", etc.
    val text: String,
    val isSelected: Boolean = false
)

enum class QuestionType {
    MULTIPLE_CHOICE,
    CHECKBOX,
    TEXT_INPUT,
    UNKNOWN
}

data class FormQuestion(
    val id: String,
    val index: Int,
    val title: String,
    val description: String = "",
    val type: QuestionType = QuestionType.MULTIPLE_CHOICE,
    val options: List<FormOption> = emptyList(),
    val isRequired: Boolean = false,
    val currentAnswer: String = ""
)

data class AiAnswerResult(
    val questionId: String,
    val questionTitle: String,
    val recommendedAnswer: String,
    val recommendedOptionIndex: Int? = null,
    val confidencePercent: Int = 95,
    val explanation: String = "",
    val eliminationNotes: List<String> = emptyList(),
    val keyConcept: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class AiChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val isFromUser: Boolean,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isStreaming: Boolean = false,
    val questionContext: String? = null
)

enum class SolverMode(val displayName: String, val description: String) {
    EXAM_MODE("Direct Answer", "Gives the exact correct answer and explanation immediately"),
    STUDY_MODE("Study & Hints", "Provides hints, Socratic questions, and explains concepts without spoiling the answer")
}

data class PresetForm(
    val title: String,
    val category: String,
    val description: String,
    val url: String,
    val isLocalPractice: Boolean = false
)
