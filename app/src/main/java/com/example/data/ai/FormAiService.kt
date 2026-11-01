package com.example.data.ai

import android.content.Context
import android.util.Log
import com.example.data.model.AiAnswerResult
import com.example.data.model.FormQuestion
import com.example.data.model.SolverMode
import com.example.util.ensureFirebaseApp
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

private const val TAG = "FormAiService"

class FormAiService(private val context: Context? = null) {

    // Structured JSON schema for question solving
    private val analysisSchema = Schema.obj(
        properties = mapOf(
            "recommendedAnswer" to Schema.string("The exact text of the correct answer or option"),
            "recommendedOptionIndex" to Schema.integer("0-based index of the correct option if multiple choice, or -1 if open ended"),
            "confidencePercent" to Schema.integer("Confidence level from 50 to 100"),
            "explanation" to Schema.string("Clear, step-by-step reasoning explaining why this answer is correct"),
            "eliminationNotes" to Schema.array(
                items = Schema.string(),
                description = "Brief notes explaining why each wrong option is incorrect"
            ),
            "keyConcept" to Schema.string("Core principle, fact, definition, or formula supporting this solution")
        ),
        optionalProperties = listOf("recommendedOptionIndex", "eliminationNotes", "keyConcept")
    )

    private val structuredModel by lazy {
        context?.let { ensureFirebaseApp(it) }
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = analysisSchema
                temperature = 0.2f
            }
        )
    }

    private val conversationalModel by lazy {
        context?.let { ensureFirebaseApp(it) }
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                temperature = 0.7f
            }
        )
    }

    /**
     * Solves a question in real-time with step-by-step reasoning and option elimination.
     */
    suspend fun analyzeQuestion(
        question: FormQuestion,
        mode: SolverMode
    ): Result<AiAnswerResult> {
        return try {
            val prompt = buildQuestionPrompt(question, mode)
            val response = withTimeout(60_000L) {
                structuredModel.generateContent(prompt)
            }

            val jsonText = response.text ?: throw IllegalStateException("AI returned an empty response")
            Log.d(TAG, "AI structured response: $jsonText")

            val json = JSONObject(jsonText)
            val recommendedAnswer = json.optString("recommendedAnswer", "")
            val rawIndex = json.optInt("recommendedOptionIndex", -1)
            val recommendedOptionIndex = if (rawIndex >= 0 && rawIndex < question.options.size) rawIndex else null
            val confidence = json.optInt("confidencePercent", 95).coerceIn(10, 100)
            val explanation = json.optString("explanation", "")
            val keyConcept = json.optString("keyConcept", "")

            val eliminationList = mutableListOf<String>()
            val eliminationArray = json.optJSONArray("eliminationNotes")
            if (eliminationArray != null) {
                for (i in 0 until eliminationArray.length()) {
                    eliminationList.add(eliminationArray.optString(i))
                }
            }

            val result = AiAnswerResult(
                questionId = question.id,
                questionTitle = question.title,
                recommendedAnswer = recommendedAnswer,
                recommendedOptionIndex = recommendedOptionIndex,
                confidencePercent = confidence,
                explanation = explanation,
                eliminationNotes = eliminationList,
                keyConcept = keyConcept
            )
            Result.success(result)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Failed to analyze question", e)
            Result.failure(e)
        }
    }

    /**
     * Streams interactive tutor responses for real-time Q&A, follow-up questions, or hints.
     */
    fun askTutorStream(
        userQuery: String,
        activeQuestion: FormQuestion?,
        mode: SolverMode,
        conversationHistory: List<Pair<String, Boolean>> // pair of (message, isUser)
    ): Flow<String> = flow {
        val promptBuilder = StringBuilder()
        promptBuilder.append("You are an expert AI academic tutor and assistant for Google Forms and test questions.\n")
        promptBuilder.append("Current mode: ${mode.displayName} (${mode.description}).\n\n")

        if (activeQuestion != null) {
            promptBuilder.append("Current form question context:\n")
            promptBuilder.append("Question: ${activeQuestion.title}\n")
            if (activeQuestion.description.isNotBlank()) {
                promptBuilder.append("Details: ${activeQuestion.description}\n")
            }
            if (activeQuestion.options.isNotEmpty()) {
                promptBuilder.append("Options:\n")
                activeQuestion.options.forEachIndexed { idx, opt ->
                    promptBuilder.append("  [${opt.label.ifBlank { "${idx + 1}" }}] ${opt.text}\n")
                }
            }
            promptBuilder.append("\n")
        }

        if (conversationHistory.isNotEmpty()) {
            promptBuilder.append("Recent conversation:\n")
            conversationHistory.takeLast(6).forEach { (msg, isUser) ->
                promptBuilder.append("${if (isUser) "Student" else "Tutor"}: $msg\n")
            }
            promptBuilder.append("\n")
        }

        promptBuilder.append("Student query: $userQuery\n\n")
        promptBuilder.append("Guidelines:\n")
        promptBuilder.append("- Provide clear, direct, and pedagogically sound responses.\n")
        promptBuilder.append("- If in Study Mode, do not immediately give away the answer letter; guide the student through deduction.\n")
        promptBuilder.append("- If in Direct Answer Mode, provide the direct answer and explain why.\n")
        promptBuilder.append("- Format with clean bullet points and bold highlights.\n")

        val accumulated = StringBuilder()
        try {
            withTimeout(60_000L) {
                conversationalModel.generateContentStream(promptBuilder.toString()).collect { chunk ->
                    val text = chunk.text.orEmpty()
                    accumulated.append(text)
                    emit(accumulated.toString())
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Tutor stream error", e)
            throw e
        }
    }

    private fun buildQuestionPrompt(question: FormQuestion, mode: SolverMode): String {
        val sb = StringBuilder()
        sb.append("You are an expert test taker and academic solver. Analyze the following Google Form question.\n\n")
        sb.append("Question: ${question.title}\n")
        if (question.description.isNotBlank()) {
            sb.append("Description/Context: ${question.description}\n")
        }
        sb.append("Type: ${question.type}\n")

        if (question.options.isNotEmpty()) {
            sb.append("\nAvailable Choices:\n")
            question.options.forEachIndexed { i, opt ->
                val label = opt.label.ifBlank { ('A' + i).toString() }
                sb.append("  Option $i [$label]: ${opt.text}\n")
            }
        } else {
            sb.append("\nThis is a short answer or free-form text question. Provide the best accurate answer.\n")
        }

        sb.append("\nInstructions:\n")
        sb.append("1. Identify the single best/correct answer based on facts, mathematics, logic, or academic consensus.\n")
        sb.append("2. In 'recommendedAnswer', provide the exact option text.\n")
        sb.append("3. In 'recommendedOptionIndex', provide the 0-based index of the chosen option (or -1 if text).\n")
        sb.append("4. In 'confidencePercent', provide your confidence score (50-100).\n")
        sb.append("5. In 'explanation', explain step-by-step why this answer is correct.\n")
        sb.append("6. In 'eliminationNotes', list specific reasons why each of the other choices is incorrect or inferior.\n")
        sb.append("7. In 'keyConcept', mention the governing formula, theorem, rule of thumb, or core concept.\n")

        if (mode == SolverMode.STUDY_MODE) {
            sb.append("\nNote: Frame the explanation to highlight learning principles and memory tricks.\n")
        }

        return sb.toString()
    }
}
