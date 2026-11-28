package com.elderassist.app.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.elderAssistDataStore by preferencesDataStore(name = "elder_assist_prefs")

enum class AppThemeMode(val label: String) {
    LIGHT("Light"),
    DARK("Dark"),
    SYSTEM("System")
}

enum class AppTextScale(val label: String, val multiplier: Float) {
    DEFAULT("Default", 1.0f),
    LARGE("Large", 1.15f),
    EXTRA_LARGE("Extra Large", 1.30f)
}

enum class SpeechSpeed(val label: String, val rate: Float) {
    SLOW("Slow", 0.78f),
    NORMAL("Normal", 0.95f)
}

enum class FeedbackLevel(val label: String) {
    EVERY_ACTION("Every action"),
    IMPORTANT_ONLY("Important only")
}

data class UserPreferences(
    val masterAiEnabled: Boolean = true,
    val experimentalAiNavigationEnabled: Boolean = true,
    val ttsEnabled: Boolean = true,
    val speechLanguageTag: String = "en-IN",
    val speechSpeed: SpeechSpeed = SpeechSpeed.NORMAL,
    val feedbackLevel: FeedbackLevel = FeedbackLevel.EVERY_ACTION,
    val themeMode: AppThemeMode = AppThemeMode.LIGHT,
    val textScale: AppTextScale = AppTextScale.DEFAULT,
    val hapticsEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val setupChecklistDismissed: Boolean = false
)

class AppSettings(private val context: Context) {
    private object Keys {
        val MASTER_AI_ENABLED = booleanPreferencesKey("master_ai_enabled")
        val EXPERIMENTAL_AI_NAV = booleanPreferencesKey("experimental_ai_nav")
        val TTS_ENABLED = booleanPreferencesKey("tts_enabled")
        val SPEECH_LANGUAGE_TAG = stringPreferencesKey("speech_language_tag")
        val SPEECH_SPEED = stringPreferencesKey("speech_speed")
        val FEEDBACK_LEVEL = stringPreferencesKey("feedback_level")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val TEXT_SCALE = stringPreferencesKey("text_scale")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val SETUP_DISMISSED = booleanPreferencesKey("setup_dismissed")
    }

    val preferences: Flow<UserPreferences> = context.elderAssistDataStore.data.map { prefs ->
        UserPreferences(
            masterAiEnabled = prefs[Keys.MASTER_AI_ENABLED] ?: true,
            experimentalAiNavigationEnabled = prefs[Keys.EXPERIMENTAL_AI_NAV] ?: true,
            ttsEnabled = prefs[Keys.TTS_ENABLED] ?: true,
            speechLanguageTag = prefs[Keys.SPEECH_LANGUAGE_TAG] ?: "en-IN",
            speechSpeed = enumOrDefault(prefs[Keys.SPEECH_SPEED], SpeechSpeed.NORMAL),
            feedbackLevel = enumOrDefault(prefs[Keys.FEEDBACK_LEVEL], FeedbackLevel.EVERY_ACTION),
            themeMode = enumOrDefault(prefs[Keys.THEME_MODE], AppThemeMode.LIGHT),
            textScale = enumOrDefault(prefs[Keys.TEXT_SCALE], AppTextScale.DEFAULT),
            hapticsEnabled = prefs[Keys.HAPTICS_ENABLED] ?: true,
            notificationsEnabled = prefs[Keys.NOTIFICATIONS_ENABLED] ?: true,
            setupChecklistDismissed = prefs[Keys.SETUP_DISMISSED] ?: false
        )
    }

    suspend fun setMasterAiEnabled(enabled: Boolean) {
        context.elderAssistDataStore.edit { it[Keys.MASTER_AI_ENABLED] = enabled }
    }

    suspend fun setExperimentalAiNavigationEnabled(enabled: Boolean) {
        context.elderAssistDataStore.edit { it[Keys.EXPERIMENTAL_AI_NAV] = enabled }
    }

    suspend fun setTtsEnabled(enabled: Boolean) {
        context.elderAssistDataStore.edit { it[Keys.TTS_ENABLED] = enabled }
    }

    suspend fun setSpeechLanguageTag(tag: String) {
        context.elderAssistDataStore.edit { it[Keys.SPEECH_LANGUAGE_TAG] = tag }
    }

    suspend fun setSpeechSpeed(speed: SpeechSpeed) {
        context.elderAssistDataStore.edit { it[Keys.SPEECH_SPEED] = speed.name }
    }

    suspend fun setFeedbackLevel(level: FeedbackLevel) {
        context.elderAssistDataStore.edit { it[Keys.FEEDBACK_LEVEL] = level.name }
    }

    suspend fun setThemeMode(mode: AppThemeMode) {
        context.elderAssistDataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setTextScale(scale: AppTextScale) {
        context.elderAssistDataStore.edit { it[Keys.TEXT_SCALE] = scale.name }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.elderAssistDataStore.edit { it[Keys.HAPTICS_ENABLED] = enabled }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.elderAssistDataStore.edit { it[Keys.NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun resetToDefaults() {
        context.elderAssistDataStore.edit { it.clear() }
    }

    private inline fun <reified T : Enum<T>> enumOrDefault(raw: String?, default: T): T {
        if (raw.isNullOrBlank()) return default
        return enumValues<T>().firstOrNull { it.name == raw } ?: default
    }
}
