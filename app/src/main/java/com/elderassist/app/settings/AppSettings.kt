package com.elderassist.app.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.elderAssistDataStore by preferencesDataStore(name = "elder_assist_prefs")

data class UserPreferences(
    val ttsEnabled: Boolean = true,
    val experimentalAiNavigationEnabled: Boolean = false,
    val speechLanguageTag: String = "en-IN",
    val setupChecklistDismissed: Boolean = false
)

class AppSettings(private val context: Context) {
    private object Keys {
        val TTS_ENABLED = booleanPreferencesKey("tts_enabled")
        val EXPERIMENTAL_AI_NAV = booleanPreferencesKey("experimental_ai_nav")
        val SPEECH_LANGUAGE_TAG = stringPreferencesKey("speech_language_tag")
        val SETUP_DISMISSED = booleanPreferencesKey("setup_dismissed")
    }

    val preferences: Flow<UserPreferences> = context.elderAssistDataStore.data.map { prefs ->
        UserPreferences(
            ttsEnabled = prefs[Keys.TTS_ENABLED] ?: true,
            experimentalAiNavigationEnabled = prefs[Keys.EXPERIMENTAL_AI_NAV] ?: false,
            speechLanguageTag = prefs[Keys.SPEECH_LANGUAGE_TAG] ?: "en-IN",
            setupChecklistDismissed = prefs[Keys.SETUP_DISMISSED] ?: false
        )
    }

    suspend fun setTtsEnabled(enabled: Boolean) {
        context.elderAssistDataStore.edit { it[Keys.TTS_ENABLED] = enabled }
    }

    suspend fun setExperimentalAiNavigationEnabled(enabled: Boolean) {
        context.elderAssistDataStore.edit { it[Keys.EXPERIMENTAL_AI_NAV] = enabled }
    }

    suspend fun setSpeechLanguageTag(tag: String) {
        context.elderAssistDataStore.edit { it[Keys.SPEECH_LANGUAGE_TAG] = tag }
    }

    suspend fun setSetupChecklistDismissed(dismissed: Boolean) {
        context.elderAssistDataStore.edit { it[Keys.SETUP_DISMISSED] = dismissed }
    }
}
