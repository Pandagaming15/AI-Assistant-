package com.example.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.FormAiService
import com.example.data.local.AppDatabase
import com.example.data.local.SavedAnswerEntity
import com.example.data.local.SavedFormEntity
import com.example.data.model.AiAnswerResult
import com.example.data.model.AiChatMessage
import com.example.data.model.FormOption
import com.example.data.model.FormQuestion
import com.example.data.model.QuestionType
import com.example.data.model.SolverMode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

private const val TAG = "FormAssistantVM"

enum class AssistantPanelMode {
    SPLIT_VIEW,
    BOTTOM_DRAWER,
    FULL_ASSISTANT
}

sealed interface AnalysisState {
    data object Idle : AnalysisState
    data class Analyzing(val questionTitle: String) : AnalysisState
    data class Success(val result: AiAnswerResult) : AnalysisState
    data class Error(val message: String) : AnalysisState
}

class FormAssistantViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val formDao = db.formDao()
    private val aiService = FormAiService()

    val savedForms: StateFlow<List<SavedFormEntity>> = formDao.getAllSavedForms()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentAnswers: StateFlow<List<SavedAnswerEntity>> = formDao.getRecentAnswers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Web navigation state
    private val _currentUrl = MutableStateFlow("file:///android_asset/practice_stem.html")
    val currentUrl: StateFlow<String> = _currentUrl.asStateFlow()

    private val _pageTitle = MutableStateFlow("Science & Technology Assessment")
    val pageTitle: StateFlow<String> = _pageTitle.asStateFlow()

    private val _pageLoadingProgress = MutableStateFlow(0)
    val pageLoadingProgress: StateFlow<Int> = _pageLoadingProgress.asStateFlow()

    private val _canGoBack = MutableStateFlow(false)
    val canGoBack: StateFlow<Boolean> = _canGoBack.asStateFlow()

    private val _canGoForward = MutableStateFlow(false)
    val canGoForward: StateFlow<Boolean> = _canGoForward.asStateFlow()

    // Form Extracted State
    private val _detectedQuestions = MutableStateFlow<List<FormQuestion>>(emptyList())
    val detectedQuestions: StateFlow<List<FormQuestion>> = _detectedQuestions.asStateFlow()

    private val _activeQuestionIndex = MutableStateFlow(0)
    val activeQuestionIndex: StateFlow<Int> = _activeQuestionIndex.asStateFlow()

    // Solutions mapped by question ID
    private val _solutionsMap = MutableStateFlow<Map<String, AiAnswerResult>>(emptyMap())
    val solutionsMap: StateFlow<Map<String, AiAnswerResult>> = _solutionsMap.asStateFlow()

    // Analysis UI State for the currently active/selected question
    private val _analysisState = MutableStateFlow<AnalysisState>(AnalysisState.Idle)
    val analysisState: StateFlow<AnalysisState> = _analysisState.asStateFlow()

    // Batch solving progress (e.g. 2 / 5)
    private val _isBatchSolving = MutableStateFlow(false)
    val isBatchSolving: StateFlow<Boolean> = _isBatchSolving.asStateFlow()

    // Modes & UI layout
    private val _solverMode = MutableStateFlow(SolverMode.EXAM_MODE)
    val solverMode: StateFlow<SolverMode> = _solverMode.asStateFlow()

    private val _panelMode = MutableStateFlow(AssistantPanelMode.SPLIT_VIEW)
    val panelMode: StateFlow<AssistantPanelMode> = _panelMode.asStateFlow()

    private val _selectedTab = MutableStateFlow(0) // 0: Live Solver, 1: All Questions, 2: AI Tutor Chat, 3: Saved History
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    // Chat messages
    private val _chatMessages = MutableStateFlow<List<AiChatMessage>>(
        listOf(
            AiChatMessage(
                isFromUser = false,
                text = "Hello! I'm your real-time Form & Quiz AI Assistant. I automatically read questions from the open form, find the correct answers with high-confidence reasoning, and can even highlight or select them for you. Ask me anything!"
            )
        )
    )
    val chatMessages: StateFlow<List<AiChatMessage>> = _chatMessages.asStateFlow()

    private val _isChatGenerating = MutableStateFlow(false)
    val isChatGenerating: StateFlow<Boolean> = _isChatGenerating.asStateFlow()

    // JS execution events for WebView (e.g. selectOption, scrollTo)
    private val _jsCommandEvents = MutableSharedFlow<String>(extraBufferCapacity = 10)
    val jsCommandEvents: SharedFlow<String> = _jsCommandEvents.asSharedFlow()

    // Floating text selection from WebView
    private val _selectedWebText = MutableStateFlow<String?>(null)
    val selectedWebText: StateFlow<String?> = _selectedWebText.asStateFlow()

    fun updateWebState(url: String, title: String, progress: Int, canBack: Boolean, canForward: Boolean) {
        _currentUrl.value = url
        _pageTitle.value = title.ifBlank { url }
        _pageLoadingProgress.value = progress
        _canGoBack.value = canBack
        _canGoForward.value = canForward

        // Record in history if HTTP/HTTPS
        if (progress == 100 && url.startsWith("http")) {
            viewModelScope.launch(Dispatchers.IO) {
                formDao.insertForm(
                    SavedFormEntity(
                        url = url,
                        title = title.ifBlank { "Google Form" },
                        lastVisitedTimestamp = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    fun loadUrl(url: String) {
        var target = url.trim()
        if (!target.startsWith("http://") && !target.startsWith("https://") && !target.startsWith("file://")) {
            target = "https://$target"
        }
        _currentUrl.value = target
        _detectedQuestions.value = emptyList()
        _activeQuestionIndex.value = 0
        _analysisState.value = AnalysisState.Idle
    }

    fun onQuestionsDetected(jsonString: String) {
        try {
            val array = JSONArray(jsonString)
            val parsed = mutableListOf<FormQuestion>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val id = obj.optString("id", "q_$i")
                val title = obj.optString("title", "")
                val description = obj.optString("description", "")
                val typeStr = obj.optString("type", "MULTIPLE_CHOICE")
                val isRequired = obj.optBoolean("isRequired", false)

                val optionsList = mutableListOf<FormOption>()
                val optArray = obj.optJSONArray("options")
                if (optArray != null) {
                    for (j in 0 until optArray.length()) {
                        val optObj = optArray.getJSONObject(j)
                        optionsList.add(
                            FormOption(
                                index = optObj.optInt("index", j),
                                label = optObj.optString("label", ('A' + j).toString()),
                                text = optObj.optString("text", ""),
                                isSelected = optObj.optBoolean("isSelected", false)
                            )
                        )
                    }
                }

                val type = when (typeStr) {
                    "CHECKBOX" -> QuestionType.CHECKBOX
                    "TEXT_INPUT" -> QuestionType.TEXT_INPUT
                    else -> QuestionType.MULTIPLE_CHOICE
                }

                parsed.add(
                    FormQuestion(
                        id = id,
                        index = i,
                        title = title,
                        description = description,
                        type = type,
                        options = optionsList,
                        isRequired = isRequired
                    )
                )
            }

            if (parsed.isNotEmpty()) {
                _detectedQuestions.value = parsed
                // If active question was reset or out of bounds
                if (_activeQuestionIndex.value >= parsed.size) {
                    _activeQuestionIndex.value = 0
                }
                // Check if current question already has a solution
                val currentQ = parsed.getOrNull(_activeQuestionIndex.value)
                if (currentQ != null && _solutionsMap.value.containsKey(currentQ.id)) {
                    _analysisState.value = AnalysisState.Success(_solutionsMap.value.getValue(currentQ.id))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing questions JSON", e)
        }
    }

    fun selectActiveQuestion(index: Int) {
        val questions = _detectedQuestions.value
        if (index in questions.indices) {
            _activeQuestionIndex.value = index
            val q = questions[index]
            val existing = _solutionsMap.value[q.id]
            if (existing != null) {
                _analysisState.value = AnalysisState.Success(existing)
            } else {
                _analysisState.value = AnalysisState.Idle
            }
            scrollToQuestionInForm(index)
        }
    }

    fun solveCurrentQuestion() {
        val questions = _detectedQuestions.value
        val index = _activeQuestionIndex.value
        if (index in questions.indices) {
            solveQuestion(questions[index])
        }
    }

    fun solveQuestion(question: FormQuestion) {
        viewModelScope.launch {
            _analysisState.value = AnalysisState.Analyzing(question.title)
            val result = aiService.analyzeQuestion(question, _solverMode.value)

            result.onSuccess { answerResult ->
                _solutionsMap.value = _solutionsMap.value + (question.id to answerResult)
                _analysisState.value = AnalysisState.Success(answerResult)

                // Save to local Room database for user review
                viewModelScope.launch(Dispatchers.IO) {
                    formDao.insertAnswer(
                        SavedAnswerEntity(
                            questionText = question.title,
                            recommendedAnswer = answerResult.recommendedAnswer,
                            explanation = answerResult.explanation,
                            keyConcept = answerResult.keyConcept,
                            formUrl = _currentUrl.value,
                            timestamp = System.currentTimeMillis()
                        )
                    )
                }
            }.onFailure { err ->
                Log.e(TAG, "Failed solving question", err)
                _analysisState.value = AnalysisState.Error(
                    err.localizedMessage ?: "Failed to generate answer. Please check network connection."
                )
            }
        }
    }

    fun solveAllDetectedQuestions() {
        val questions = _detectedQuestions.value
        if (questions.isEmpty()) return

        viewModelScope.launch {
            _isBatchSolving.value = true
            for (q in questions) {
                if (!_solutionsMap.value.containsKey(q.id)) {
                    val result = aiService.analyzeQuestion(q, _solverMode.value)
                    result.onSuccess { sol ->
                        _solutionsMap.value = _solutionsMap.value + (q.id to sol)
                        // If it's currently selected question, update analysis state
                        if (_detectedQuestions.value.getOrNull(_activeQuestionIndex.value)?.id == q.id) {
                            _analysisState.value = AnalysisState.Success(sol)
                        }
                    }
                }
            }
            _isBatchSolving.value = false
        }
    }

    fun autoSelectInForm(questionIndex: Int, optionIndex: Int) {
        _jsCommandEvents.tryEmit("window.formAssistantSelectOption($questionIndex, $optionIndex);")
    }

    fun scrollToQuestionInForm(questionIndex: Int) {
        _jsCommandEvents.tryEmit("window.formAssistantScrollTo($questionIndex);")
    }

    fun sendChatMessage(userText: String) {
        if (userText.isBlank() || _isChatGenerating.value) return

        val activeQ = _detectedQuestions.value.getOrNull(_activeQuestionIndex.value)
        val userMsg = AiChatMessage(
            isFromUser = true,
            text = userText.trim(),
            questionContext = activeQ?.title
        )

        val updatedList = _chatMessages.value + userMsg
        _chatMessages.value = updatedList
        _isChatGenerating.value = true

        val assistantMsgId = java.util.UUID.randomUUID().toString()
        val initialAssistantMsg = AiChatMessage(
            id = assistantMsgId,
            isFromUser = false,
            text = "Thinking...",
            isStreaming = true
        )
        _chatMessages.value = updatedList + initialAssistantMsg

        val history = updatedList.map { it.text to it.isFromUser }

        viewModelScope.launch {
            try {
                aiService.askTutorStream(
                    userQuery = userText,
                    activeQuestion = activeQ,
                    mode = _solverMode.value,
                    conversationHistory = history
                ).collect { accumulated ->
                    _chatMessages.value = _chatMessages.value.map { msg ->
                        if (msg.id == assistantMsgId) {
                            msg.copy(text = accumulated, isStreaming = true)
                        } else msg
                    }
                }
                // Mark streaming complete
                _chatMessages.value = _chatMessages.value.map { msg ->
                    if (msg.id == assistantMsgId) {
                        msg.copy(isStreaming = false)
                    } else msg
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Chat failed", e)
                _chatMessages.value = _chatMessages.value.map { msg ->
                    if (msg.id == assistantMsgId) {
                        msg.copy(
                            text = "Sorry, I encountered an issue: ${e.localizedMessage ?: "Request failed"}",
                            isStreaming = false
                        )
                    } else msg
                }
            } finally {
                _isChatGenerating.value = false
            }
        }
    }

    fun onTextSelectedInWeb(text: String) {
        if (text.isNotBlank()) {
            _selectedWebText.value = text
        }
    }

    fun clearSelectedWebText() {
        _selectedWebText.value = null
    }

    fun askAboutSelectedText() {
        val text = _selectedWebText.value ?: return
        _selectedTab.value = 2 // Switch to chat
        sendChatMessage("Explain or answer this selected text from the form: \"$text\"")
        _selectedWebText.value = null
    }

    fun setSolverMode(mode: SolverMode) {
        _solverMode.value = mode
    }

    fun setPanelMode(mode: AssistantPanelMode) {
        _panelMode.value = mode
    }

    fun setSelectedTab(tab: Int) {
        _selectedTab.value = tab
    }

    fun toggleFavorite(url: String, isFav: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            formDao.updateFavorite(url, isFav)
        }
    }

    fun deleteSavedForm(url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            formDao.deleteForm(url)
        }
    }

    fun clearAnswerHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            formDao.clearAnswerHistory()
        }
    }
}
