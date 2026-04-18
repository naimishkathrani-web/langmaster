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
import com.langmaster.data.local.entity.UserEntity
import com.langmaster.data.repo.AgentTranslateRepository
import com.langmaster.data.repo.ChatRepository
import com.langmaster.data.repo.LearningRepository
import com.langmaster.data.repo.TranslationPolicyEvaluator
import com.langmaster.data.service.AuthService
import com.langmaster.data.service.LocalDevAuthService
import com.langmaster.data.service.GoogleTranslateService
import com.langmaster.data.service.TranslationService
import com.langmaster.data.service.LearningService
import com.langmaster.data.service.LocalLearningService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import androidx.room.withTransaction

class LangMasterViewModel(application: Application) : AndroidViewModel(application) {
    private val db = DatabaseProvider.get(application)
    private val chatRepository = ChatRepository(db)
    private val translationRepository = AgentTranslateRepository(db)
    private val learningRepository = LearningRepository(db)
    private val authService: AuthService = LocalDevAuthService(db)
    private val translationService: TranslationService = GoogleTranslateService()
    private val learningService: LearningService = LocalLearningService()

    private var localUserPhone = ""
    val currentUserPhone = MutableStateFlow<String>("")
    // Note: since the backend doesn't exist, we don't need a contactPhone constant anymore.
    private val _activeConversationId = MutableStateFlow<String?>(null)
    val activeConversationId: StateFlow<String?> = _activeConversationId

    val isLoggedIn = MutableStateFlow(false)
    val authStatus = MutableStateFlow<String?>(null)
    
    val contacts = MutableStateFlow<List<UserEntity>>(emptyList())

    fun clearAuthStatus() {
        authStatus.value = null
    }

    val conversations: StateFlow<List<ConversationEntity>> = chatRepository.observeConversations().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    val messages: StateFlow<List<MessageEntity>> = _activeConversationId.flatMapLatest { id ->
        if (id != null) chatRepository.observeMessages(id) else kotlinx.coroutines.flow.flowOf(emptyList())
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5_000), initialValue = emptyList())

    val activeConversationTitle: StateFlow<String> = combine(conversations, _activeConversationId) { list, selectedId ->
        list.firstOrNull { it.id == selectedId }?.title ?: "Chat"
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "Chat")

    val learningLanguage = MutableStateFlow("English")
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
        android.util.Log.d("LangMasterVM", "LangMasterViewModel initialized")
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                db.withTransaction {
                    android.util.Log.d("LangMasterVM", "Starting DB seeding inside transaction")
                    if (localUserPhone.isNotBlank()) {
                        chatRepository.seedSimulatedData(localUserPhone)
                        contacts.value = db.userDao().getAllOtherUsers(localUserPhone)
                    }
                    learningRepository.seedTracks("English")
                    android.util.Log.d("LangMasterVM", "DB seeding completed inside transaction")
                }
            } catch (e: Exception) {
                android.util.Log.e("LangMasterVM", "DB seeding failed", e)
            }
        }
    }

    fun setListenerPreference(enabled: Boolean, preferredLanguage: String) {
        val convId = _activeConversationId.value ?: return
        viewModelScope.launch {
            chatRepository.upsertMemberPreference(
                conversationId = convId,
                phone = localUserPhone,
                translationEnabled = enabled,
                preferredLanguage = preferredLanguage
            )
        }
    }

    fun sendConnectMessage(text: String, language: String) {
        if (text.isBlank()) return
        val currentId = _activeConversationId.value ?: return
        viewModelScope.launch {
            chatRepository.sendTextMessage(currentId, localUserPhone, text, language)
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

    fun selectConversation(conversationId: String?) {
        _activeConversationId.value = conversationId
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

    fun register(
        phone: String,
        email: String,
        pin: String,
        confirmPin: String,
        firstName: String,
        lastName: String,
        nativeLanguage: String,
        otherLanguages: List<String>
    ) {
        viewModelScope.launch {
            if (pin != confirmPin) {
                authStatus.value = "PIN mismatch. Please re-enter."
                return@launch
            }
            if (!pin.matches(Regex("^[0-9]{4}$"))) {
                authStatus.value = "PIN must be exactly 4 digits."
                return@launch
            }
            if (firstName.isBlank() || lastName.isBlank()) {
                authStatus.value = "First Name and Last Name are required."
                return@launch
            }
            
            // Note: register doesn't currently take first/last directly in the old code, 
            // but we can bypass the simple AuthService and write native DB registration here
            // since we are fully local now.
            try {
                if (db.userDao().getUserByPhone(phone) != null) {
                    authStatus.value = "User already exists."
                    return@launch
                }
                
                db.userDao().upsert(
                    UserEntity(
                        id = java.util.UUID.randomUUID().toString(),
                        phoneE164 = phone,
                        displayName = "$firstName $lastName",
                        pin = pin,
                        googleAccountEmail = email,
                        nativeLanguage = nativeLanguage,
                        otherLanguages = otherLanguages.joinToString(","),
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                )
                authStatus.value = "Registration successful. Logging in..."
                loginWithPin(phone, pin)
            } catch (e: Exception) {
                authStatus.value = "Database error: ${e.message}"
            }
        }
    }

    fun loginWithPin(phone: String, pin: String) {
        viewModelScope.launch {
            val result = authService.loginWithPin(phone, pin)
            if (result.ok) {
                localUserPhone = phone
                currentUserPhone.value = phone
                isLoggedIn.value = true
                authStatus.value = "Login successful."
                
                // Re-seed with proper phone now that we know who logged in
                db.withTransaction {
                    chatRepository.seedSimulatedData(localUserPhone)
                    contacts.value = db.userDao().getAllOtherUsers(localUserPhone)
                }
            } else {
                authStatus.value = result.error ?: "Invalid credentials"
            }
        }
    }
}
