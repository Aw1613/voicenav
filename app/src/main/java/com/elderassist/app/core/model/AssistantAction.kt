package com.elderassist.app.core.model

enum class SettingDestination(val displayName: String) {
    WIFI("Wi-Fi settings"),
    BLUETOOTH("Bluetooth settings"),
    HOTSPOT("Hotspot and tethering settings"),
    MOBILE_DATA("Mobile data settings"),
    AIRPLANE_MODE("Airplane mode settings"),
    DISPLAY("Display settings"),
    SOUND("Sound settings"),
    ACCESSIBILITY("Accessibility settings"),
    BATTERY("Battery settings"),
    GENERAL_SETTINGS("Phone settings")
}

enum class SupportedApp(val displayName: String) {
    CONTACTS("Contacts"),
    SETTINGS("Settings")
}

enum class ScrollDirection {
    UP,
    DOWN,
    LEFT,
    RIGHT
}

enum class AssistantMode {
    DETERMINISTIC,
    AI_EXPERIMENTAL
}

enum class RiskLevel {
    LOW,
    MEDIUM,
    HIGH_PRIVACY,
    CONSEQUENTIAL_WRITE,
    PROHIBITED
}

enum class ActionSource {
    DETERMINISTIC,
    GEMINI_INTENT,
    GEMINI_NAVIGATION
}

data class ClarificationOption(
    val label: String,
    val canonicalUtterance: String
)

sealed interface AssistantAction {
    data class OpenSetting(
        val destination: SettingDestination
    ) : AssistantAction

    data class OpenApp(
        val app: SupportedApp
    ) : AssistantAction

    data class OpenNamedApp(
        val appName: String,
        val packageName: String? = null
    ) : AssistantAction

    data class PlaceCall(
        val recipient: String
    ) : AssistantAction

    data class PlaceWhatsAppCall(
        val recipient: String,
        val isVideo: Boolean = false
    ) : AssistantAction

    data object GoHome : AssistantAction
    data object GoBack : AssistantAction
    data object OpenRecents : AssistantAction
    data object Cancel : AssistantAction

    data class SearchContact(
        val query: String
    ) : AssistantAction

    data class CreateContact(
        val displayName: String,
        val phoneNumber: String
    ) : AssistantAction

    data class RenameContact(
        val contactId: Long = -1L,
        val lookupKey: String = "",
        val currentDisplayName: String,
        val newDisplayName: String
    ) : AssistantAction

    data class ClickNode(
        val snapshotId: String,
        val nodeId: String,
        val descriptionHint: String? = null
    ) : AssistantAction

    data class Scroll(
        val snapshotId: String,
        val nodeId: String?,
        val direction: ScrollDirection
    ) : AssistantAction

    data class SetNodeText(
        val snapshotId: String,
        val nodeId: String,
        val text: String
    ) : AssistantAction

    data class AskClarification(
        val question: String,
        val options: List<ClarificationOption> = emptyList()
    ) : AssistantAction

    data class Explain(
        val message: String,
        val guidanceDestination: SettingDestination? = null
    ) : AssistantAction

    data class Unsupported(
        val message: String,
        val fallbackDestination: SettingDestination? = null
    ) : AssistantAction
}
