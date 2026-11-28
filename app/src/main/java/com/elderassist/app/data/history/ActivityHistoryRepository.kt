package com.elderassist.app.data.history

import android.content.Context
import androidx.room.Room
import com.elderassist.app.core.model.ActionId
import com.elderassist.app.core.model.AssistantAction
import com.elderassist.app.core.model.SettingDestination
import com.elderassist.app.core.model.SupportedApp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

data class ActivityEvent(
    val id: String,
    val occurredAtEpochMs: Long,
    val actionCode: String,
    val outcome: ActivityOutcome,
    val userSummary: String,
    val detail: String?
)

/**
 * Stores strictly sanitized activity records in Room.
 * Never persists raw phone numbers, contact names on writes, transcripts, or screen dumps.
 */
class ActivityHistoryRepository(
    private val dao: ActivityEventDao
) {
    val events: Flow<List<ActivityEvent>> = dao.observeRecent().map { list ->
        list.map { entity ->
            ActivityEvent(
                id = entity.id,
                occurredAtEpochMs = entity.occurredAtEpochMs,
                actionCode = entity.actionCode,
                outcome = ActivityOutcome.entries.firstOrNull { it.name == entity.outcome }
                    ?: ActivityOutcome.INFO,
                userSummary = entity.userSummary,
                detail = entity.detail
            )
        }
    }

    suspend fun record(
        actionCode: String,
        outcome: ActivityOutcome,
        userSummary: String,
        detail: String? = null
    ) {
        val sanitizedSummary = sanitizeText(userSummary).take(120)
        val sanitizedDetail = detail?.let { sanitizeText(it).take(180) }
        dao.insert(
            ActivityEventEntity(
                id = UUID.randomUUID().toString(),
                occurredAtEpochMs = System.currentTimeMillis(),
                actionCode = actionCode,
                outcome = outcome.name,
                userSummary = sanitizedSummary,
                detail = sanitizedDetail
            )
        )
    }

    suspend fun recordActionOutcome(
        action: AssistantAction,
        outcome: ActivityOutcome,
        detail: String? = null
    ) {
        val (code, summary) = sanitizedMetadataFor(action, outcome)
        record(
            actionCode = code,
            outcome = outcome,
            userSummary = summary,
            detail = detail
        )
    }

    suspend fun clearHistory() {
        dao.clearAll()
    }

    companion object {
        private val phoneDigitsRegex = Regex("""\+?\d[\d\s\-]{4,}\d""")

        fun create(context: Context): ActivityHistoryRepository {
            val db = Room.databaseBuilder(
                context.applicationContext,
                ElderAssistDatabase::class.java,
                "elder_assist_history.db"
            ).build()
            return ActivityHistoryRepository(db.activityEventDao())
        }

        fun sanitizeText(input: String): String {
            return input.replace(phoneDigitsRegex, "[redacted number]")
        }

        fun sanitizedMetadataFor(
            action: AssistantAction,
            outcome: ActivityOutcome
        ): Pair<String, String> = when (action) {
            is AssistantAction.OpenSetting -> when (action.destination) {
                SettingDestination.WIFI -> ActionId.OPEN_WIFI.code to "Opened Wi-Fi settings"
                SettingDestination.BLUETOOTH -> ActionId.OPEN_BLUETOOTH.code to "Opened Bluetooth settings"
                SettingDestination.HOTSPOT -> ActionId.OPEN_HOTSPOT.code to "Opened Hotspot settings"
                SettingDestination.DISPLAY -> ActionId.OPEN_DISPLAY.code to "Opened Display settings"
                SettingDestination.SOUND -> ActionId.OPEN_SOUND.code to "Opened Sound settings"
                SettingDestination.ACCESSIBILITY -> ActionId.OPEN_ACCESSIBILITY.code to "Opened Accessibility settings"
                SettingDestination.BATTERY -> ActionId.OPEN_BATTERY.code to "Opened Battery settings"
                SettingDestination.GENERAL_SETTINGS -> "ACT-SET" to "Opened Phone settings"
            }

            is AssistantAction.OpenApp -> when (action.app) {
                SupportedApp.CONTACTS -> ActionId.OPEN_CONTACTS.code to "Opened Contacts"
                SupportedApp.SETTINGS -> "ACT-SET" to "Opened Phone settings"
            }

            is AssistantAction.OpenNamedApp -> "ACT-APP" to "Opened ${action.appName}"

            is AssistantAction.PlaceCall -> "ACT-CALL" to "Called ${action.recipient}"

            AssistantAction.GoHome -> ActionId.GO_HOME.code to "Returned to Home screen"
            AssistantAction.GoBack -> ActionId.GO_BACK.code to "Navigated Back"
            AssistantAction.OpenRecents -> ActionId.OPEN_RECENTS.code to "Opened Recent apps"
            AssistantAction.Cancel -> "ACT-CANCEL" to "Stopped active task"

            is AssistantAction.SearchContact -> ActionId.SEARCH_CONTACT.code to
                if (outcome == ActivityOutcome.SUCCESS) "Searched contacts" else "Contact search finished"

            is AssistantAction.CreateContact -> ActionId.CREATE_CONTACT.code to
                when (outcome) {
                    ActivityOutcome.SUCCESS -> "Created a contact"
                    ActivityOutcome.CANCELLED -> "Cancelled adding a contact"
                    else -> "Attempted to add a contact"
                }

            is AssistantAction.RenameContact -> ActionId.RENAME_CONTACT.code to
                when (outcome) {
                    ActivityOutcome.SUCCESS -> "Renamed a contact"
                    ActivityOutcome.CANCELLED -> "Cancelled renaming a contact"
                    else -> "Attempted to rename a contact"
                }

            is AssistantAction.ClickNode,
            is AssistantAction.Scroll,
            is AssistantAction.SetNodeText -> "ACT-GUIDE" to "Guided screen navigation step"

            is AssistantAction.AskClarification -> "ACT-CLARIFY" to "Asked a clarifying question"
            is AssistantAction.Explain -> "ACT-EXPLAIN" to "Shared helpful guidance"
            is AssistantAction.Unsupported -> "ACT-BLOCKED" to "Blocked an unsupported action"
        }
    }
}
