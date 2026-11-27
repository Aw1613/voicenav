package com.elderassist.app

import android.app.Application
import android.content.Context
import com.elderassist.app.accessibility.AccessibilityExecutor
import com.elderassist.app.accessibility.AccessibilityTreeReader
import com.elderassist.app.data.history.ActivityHistoryRepository
import com.elderassist.app.execution.ActionDispatcher
import com.elderassist.app.execution.ActionValidator
import com.elderassist.app.execution.ActionVerifier
import com.elderassist.app.execution.IntentExecutor
import com.elderassist.app.execution.TaskRunner
import com.elderassist.app.feature.contacts.ContactsActionHandler
import com.elderassist.app.feature.contacts.ContactsRepository
import com.elderassist.app.feature.settings.SettingsActionHandler
import com.elderassist.app.feature.voice.AndroidSpeechRecognizerProvider
import com.elderassist.app.feature.voice.TextToSpeechSpeaker
import com.elderassist.app.feature.voice.VoiceSessionManager
import com.elderassist.app.intelligence.CommandParser
import com.elderassist.app.intelligence.DefaultActionPlanner
import com.elderassist.app.intelligence.DefaultIntentRouter
import com.elderassist.app.intelligence.GeminiClient
import com.elderassist.app.intelligence.GeminiIntentInterpreter
import com.elderassist.app.intelligence.GeneralNavigationAgent
import com.elderassist.app.intelligence.HttpGeminiClient
import com.elderassist.app.permissions.PermissionStateRepository
import com.elderassist.app.settings.AppSettings

class AppContainer(context: Context) {
    val appSettings = AppSettings(context)
    val historyRepository = ActivityHistoryRepository.create(context)

    private val speechProvider = AndroidSpeechRecognizerProvider(context)
    private val ttsSpeaker = TextToSpeechSpeaker(context)
    val voiceSessionManager = VoiceSessionManager(speechProvider, ttsSpeaker)

    val intentExecutor = IntentExecutor(context)
    private val settingsHandler = SettingsActionHandler(intentExecutor)

    val contactsRepository = ContactsRepository(context)
    private val contactsHandler = ContactsActionHandler(contactsRepository, intentExecutor)

    private val treeReader = AccessibilityTreeReader()
    private val accessibilityExecutor = AccessibilityExecutor(treeReader)

    val actionValidator = ActionValidator()
    val actionPlanner = DefaultActionPlanner()
    val intentRouter = DefaultIntentRouter(CommandParser())

    private val actionDispatcher = ActionDispatcher(
        validator = actionValidator,
        settingsHandler = settingsHandler,
        contactsHandler = contactsHandler,
        accessibilityExecutor = accessibilityExecutor
    )
    val taskRunner = TaskRunner(actionDispatcher, ActionVerifier())

    val geminiClient: GeminiClient = HttpGeminiClient()
    val geminiIntentInterpreter = GeminiIntentInterpreter(geminiClient)
    val generalNavigationAgent = GeneralNavigationAgent(
        geminiClient = geminiClient,
        actionPlanner = actionPlanner,
        taskRunner = taskRunner
    )

    val permissionRepository = PermissionStateRepository(
        context = context,
        isGeminiConfigured = { geminiClient.isConfigured() }
    )
}

class ElderAssistApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
