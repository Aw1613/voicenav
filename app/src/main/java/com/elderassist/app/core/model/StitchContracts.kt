package com.elderassist.app.core.model

object StitchIds {
    const val SCR_01 = "SCR-01"
    const val SCR_02 = "SCR-02"
    const val SCR_03 = "SCR-03"
    const val SCR_04 = "SCR-04"
    const val SCR_05 = "SCR-05"
    const val SCR_06 = "SCR-06"
    const val SCR_07 = "SCR-07"
    const val SCR_08 = "SCR-08"
    const val SCR_09 = "SCR-09"

    const val CMP_01 = "CMP-01"
    const val CMP_02 = "CMP-02"
    const val CMP_05 = "CMP-05"
    const val CMP_06 = "CMP-06"
    const val CMP_07 = "CMP-07"

    const val STA_01 = "STA-01"
    const val STA_02 = "STA-02"
    const val STA_03 = "STA-03"
    const val STA_04 = "STA-04"
    const val STA_05 = "STA-05"
    const val STA_06 = "STA-06"
    const val STA_07 = "STA-07"
    const val STA_08 = "STA-08"
    const val STA_09 = "STA-09"
}

enum class ElderAssistRoute(
    val route: String,
    val routeId: String,
    val screenId: String,
    val label: String
) {
    HOME("home", "RTE-01", StitchIds.SCR_01, "Voice Guide"),
    MASTER_AI("master_ai", "RTE-02", StitchIds.SCR_02, "Master AI"),
    SETTINGS_NAV("settings_navigation", "RTE-03", StitchIds.SCR_03, "Settings & Navigation"),
    CONTACTS("contacts", "RTE-04", StitchIds.SCR_04, "Contacts"),
    VOICE_LANGUAGE("voice_language", "RTE-05", StitchIds.SCR_05, "Voice & Language"),
    PRIVACY("privacy", "RTE-06", StitchIds.SCR_06, "Permissions & Privacy"),
    HISTORY("history", "RTE-07", StitchIds.SCR_07, "Activity History"),
    HELP("help", "RTE-08", StitchIds.SCR_08, "Help & Tutorials"),
    APP_SETTINGS("app_settings", "RTE-09", StitchIds.SCR_09, "App Settings")
}

enum class ActionId(val code: String) {
    OPEN_WIFI("ACT-01"),
    OPEN_BLUETOOTH("ACT-02"),
    OPEN_HOTSPOT("ACT-03"),
    OPEN_DISPLAY("ACT-04"),
    OPEN_SOUND("ACT-05"),
    OPEN_ACCESSIBILITY("ACT-06"),
    OPEN_BATTERY("ACT-07"),
    OPEN_CONTACTS("ACT-08"),
    SEARCH_CONTACT("ACT-09"),
    CREATE_CONTACT("ACT-10"),
    RENAME_CONTACT("ACT-11"),
    TEST_MIC("ACT-12"),
    GO_HOME("ACT-13"),
    GO_BACK("ACT-14"),
    OPEN_RECENTS("ACT-15")
}

enum class Capability {
    MICROPHONE,
    CONTACTS_READ,
    ACCESSIBILITY,
    INTERNET_AI
}

enum class Recovery {
    NONE,
    RETRY_VOICE,
    OPEN_PERMISSIONS,
    OPEN_ACCESSIBILITY_SETTINGS,
    OPEN_FALLBACK_SETTINGS
}

data class ContactMatch(
    val lookupKey: String,
    val displayName: String,
    val maskedNumber: String? = null
)

sealed interface ContactProposal {
    data class Create(
        val displayName: String,
        val phone: String
    ) : ContactProposal

    data class Rename(
        val contactId: Long,
        val lookupKey: String,
        val oldName: String,
        val newName: String
    ) : ContactProposal
}

sealed interface AssistantUiState {
    val stateId: String

    data object Idle : AssistantUiState {
        override val stateId: String = StitchIds.STA_01
    }

    data class NeedsSetup(val missing: Set<Capability>) : AssistantUiState {
        override val stateId: String = StitchIds.STA_02
    }

    data class Listening(val partial: String) : AssistantUiState {
        override val stateId: String = StitchIds.STA_03
    }

    data class Processing(val request: String) : AssistantUiState {
        override val stateId: String = StitchIds.STA_04
    }

    data class Executing(val description: String) : AssistantUiState {
        override val stateId: String = StitchIds.STA_05
    }

    data class Confirmation(val proposal: ContactProposal) : AssistantUiState {
        override val stateId: String = StitchIds.STA_06
    }

    data class Success(val message: String) : AssistantUiState {
        override val stateId: String = StitchIds.STA_07
    }

    data class Failure(
        val userMessage: String,
        val recovery: Recovery = Recovery.NONE
    ) : AssistantUiState {
        override val stateId: String = StitchIds.STA_08
    }
}
