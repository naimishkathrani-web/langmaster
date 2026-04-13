package com.langmaster.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.langmaster.BuildConfig
import com.langmaster.config.AppConfig
import com.langmaster.data.local.DatabaseProvider
import com.langmaster.data.local.entity.ConversationEntity
import com.langmaster.data.local.entity.LearningModuleEntity
import com.langmaster.data.local.entity.LearningTrackEntity
import com.langmaster.data.local.entity.MessageEntity
import com.langmaster.data.local.entity.TranslationSessionEntity
import com.langmaster.data.repo.AgentTranslateRepository
import com.langmaster.data.repo.ChatRepository
import com.langmaster.data.repo.LearningRepository
import com.langmaster.data.repo.TranslationPolicyEvaluator
import com.langmaster.data.service.AuthService
import com.langmaster.data.service.BackendAuthService
import com.langmaster.data.service.LocalDevAuthService
import com.langmaster.data.service.LocalTranslationService
import com.langmaster.data.service.BackendTranslationService
import com.langmaster.data.service.TranslationService
import com.langmaster.data.service.BackendLearningService
import com.langmaster.data.service.LearningService
import com.langmaster.data.service.LocalLearningService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LangMasterViewModel(application: Application) : AndroidViewModel(application) {
    private val db = DatabaseProvider.get(application)
    private val chatRepository = ChatRepository(db)
    private val translationRepository = AgentTranslateRepository(db)
    private val learningRepository = LearningRepository(db)
    private val authService: AuthService = if (BuildConfig.DEBUG) {
        LocalDevAuthService()
    } else {
        BackendAuthService(AppConfig.apiBaseUrl)
    }
    private val translationService: TranslationService = if (BuildConfig.DEBUG) {
        LocalTranslationService()
    } else {
        BackendTranslationService(AppConfig.apiBaseUrl)
    }
    private val learningService: LearningService = if (BuildConfig.DEBUG) {
        LocalLearningService()
    } else {
        BackendLearningService(AppConfig.apiBaseUrl)
    }

    private val localUserPhone = "+911111111111"
    private val contactPhone = "+919999999999"
    private val activeConversationId = MutableStateFlow("conv-ravi")
    val isLoggedIn = MutableStateFlow(false)
    val authStatus = MutableStateFlow<String?>(null)

    val conversations: StateFlow<List<ConversationEntity>> = chatRepository.observeConversations().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    val messages: StateFlow<List<MessageEntity>> = activeConversationId.flatMapLatest {
        chatRepository.observeMessages(it)
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5_000), initialValue = emptyList())

    val activeConversationTitle: StateFlow<String> = combine(conversations, activeConversationId) { list, selectedId ->
        list.firstOrNull { it.id == selectedId }?.title ?: "Conversation"
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "Conversation")

    private val learningLanguage = MutableStateFlow("English")
    val learningTracks: StateFlow<List<LearningTrackEntity>> = learningLanguage.flatMapLatest {
        learningRepository.observeTracks(it)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val activeTrackId = MutableStateFlow("track-English-beginner")
    val learningModules: StateFlow<List<LearningModuleEntity>> = activeTrackId.flatMapLatest {
        learningRepository.observeModules(it)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val translationSessions: StateFlow<List<TranslationSessionEntity>> =
        translationRepository.observeSessions(localUserPhone).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch {
            chatRepository.seedDefaultConversation(activeConversationId.value, localUserPhone, contactPhone)
            chatRepository.seedGroupConversation(
                conversationId = "conv-group-1",
                localUserPhone = localUserPhone,
                memberPhones = listOf("+918888888888", "+917777777777", "+916666666666")
            )
            if (messages.value.isEmpty()) {
                chatRepository.sendTextMessage(activeConversationId.value, contactPhone, "Namaste!", "Hindi")
            }
            learningRepository.seedTracks("English")
        }
    }

    fun setListenerPreference(enabled: Boolean, preferredLanguage: String) {
        viewModelScope.launch {
            chatRepository.upsertMemberPreference(
                conversationId = activeConversationId.value,
                phone = localUserPhone,
                translationEnabled = enabled,
                preferredLanguage = preferredLanguage
            )
        }
    }

    fun sendConnectMessage(text: String, language: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            chatRepository.sendTextMessage(activeConversationId.value, localUserPhone, text, language)
        }
    }

    fun deleteForEveryone(message: MessageEntity) {
        viewModelScope.launch {
            chatRepository.deleteForEveryoneWith24hWindow(message.id, message.createdAt)
        }
    }

    fun saveTranslation(source: String, target: String, input: String) {
        viewModelScope.launch {
            val output = translationService.translateText(source, target, input)
            translationRepository.saveTextSession(
                userId = localUserPhone,
                sourceLanguage = source,
                targetLanguage = target,
                inputText = input,
                outputText = output
            )
        }
    }

    fun shouldUseAi(translationEnabled: Boolean, listenerLanguage: String, sourceLanguage: String): Boolean {
        return TranslationPolicyEvaluator.shouldUseAi(translationEnabled, listenerLanguage, sourceLanguage)
    }

    fun setLearningLanguage(language: String) {
        learningLanguage.value = language
        activeTrackId.value = "track-$language-beginner"
        viewModelScope.launch {
            val remoteModules = learningService.fetchModules(language)
            if (remoteModules.isNotEmpty()) {
                learningRepository.seedTracksFromService(language, remoteModules)
            } else {
                learningRepository.seedTracks(language)
            }
        }
    }

    fun markModuleProgress(moduleId: String, scorePercent: Int) {
        viewModelScope.launch {
            learningRepository.updateProgress(localUserPhone, moduleId, scorePercent)
        }
    }

    fun selectConversation(conversationId: String) {
        activeConversationId.value = conversationId
    }

    fun deleteForMe(messageId: String) {
        viewModelScope.launch {
            chatRepository.deleteForMe(messageId)
        }
    }

    fun forwardMessage(message: MessageEntity, targetConversationId: String) {
        viewModelScope.launch {
            chatRepository.forwardMessage(message, targetConversationId, localUserPhone)
        }
    }

    fun registerPin(phone: String, pin: String, confirmPin: String) {
        viewModelScope.launch {
            if (pin != confirmPin) {
                authStatus.value = "PIN mismatch. Please re-enter."
                return@launch
            }
            if (!pin.matches(Regex("^[0-9]{4}$"))) {
                authStatus.value = "PIN must be exactly 4 digits."
                return@launch
            }
            val response = authService.registerPin(phone, pin)
            authStatus.value = if (response.ok) "PIN registered. You can log in now." else (response.error ?: "Registration failed")
        }
    }

    fun loginWithPin(phone: String, pin: String) {
        viewModelScope.launch {
            val result = authService.loginWithPin(phone, pin)
            isLoggedIn.value = result.ok
            authStatus.value = if (result.ok) "Login successful." else (result.error ?: "Invalid credentials")
        }
    }
}
