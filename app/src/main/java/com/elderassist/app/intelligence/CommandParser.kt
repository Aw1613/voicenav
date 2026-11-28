package com.elderassist.app.intelligence

import com.elderassist.app.core.model.AssistantAction
import com.elderassist.app.core.model.ClarificationOption
import com.elderassist.app.core.model.SettingDestination
import com.elderassist.app.core.model.SupportedApp

sealed interface ParseOutcome {
    data object Empty : ParseOutcome
    data class Matched(
        val action: AssistantAction,
        val summaryForUser: String
    ) : ParseOutcome

    data class Ambiguous(
        val question: String,
        val options: List<ClarificationOption>
    ) : ParseOutcome

    data class ProhibitedOrUnsupported(
        val message: String,
        val fallbackDestination: SettingDestination? = null
    ) : ParseOutcome

    data class Unmatched(val normalizedTranscript: String) : ParseOutcome
}

class CommandParser {

    fun normalize(rawTranscript: String): String {
        return rawTranscript
            .trim()
            .lowercase()
            .replace(Regex("""[?!.,;:'"]+"""), " ")
            .replace(Regex("""\s+"""), " ")
            .trim()
    }

    private fun stripPolitePrefix(text: String): String {
        var result = text
        val prefixes = listOf(
            "hey elderassist ",
            "ok elderassist ",
            "please ",
            "kindly ",
            "can you ",
            "could you ",
            "would you ",
            "help me ",
            "i want to ",
            "i would like to ",
            "take me to ",
            "show me ",
            "open the ",
            "open "
        )
        var changed = true
        while (changed) {
            changed = false
            for (prefix in prefixes) {
                if (result.startsWith(prefix)) {
                    result = result.removePrefix(prefix).trim()
                    changed = true
                }
            }
        }
        return result
    }

    fun parse(rawTranscript: String): ParseOutcome {
        val normalized = normalize(rawTranscript)
        if (normalized.isEmpty()) return ParseOutcome.Empty

        // 1. Cancellation phrases always take top priority.
        if (isCancelCommand(normalized)) {
            return ParseOutcome.Matched(
                action = AssistantAction.Cancel,
                summaryForUser = "Cancelled."
            )
        }

        // 2. Prohibited / high-risk safety checks before any entity matching.
        checkProhibitedOrUnsupported(normalized)?.let { return it }

        // 3. Ambiguous connection / generic requests.
        checkAmbiguous(normalized)?.let { return it }

        // 4. System navigation commands.
        parseSystemNavigation(normalized)?.let { return it }

        // 5. Contacts commands (create, rename, search, open).
        parseContactsCommand(normalized)?.let { return it }

        // 6. Seven supported Settings destinations + general settings.
        parseSettingsCommand(normalized)?.let { return it }

        // 7. Open any installed app (e.g. WhatsApp, Messages, Call Log, Camera, YouTube).
        parseAppCommand(normalized)?.let { return it }

        return ParseOutcome.Unmatched(normalized)
    }

    private fun isCancelCommand(text: String): Boolean {
        val exactCancels = setOf(
            "stop",
            "cancel",
            "never mind",
            "nevermind",
            "abort",
            "stop doing that",
            "cancel task",
            "cancel that",
            "stop it",
            "ruk jao",
            "band karo"
        )
        if (text in exactCancels) return true
        return text.startsWith("stop ") && (text.contains("task") || text.contains("doing"))
    }

    private fun checkProhibitedOrUnsupported(text: String): ParseOutcome.ProhibitedOrUnsupported? {
        if (Regex("""\b(delete|remove|erase|wipe)\b""").containsMatchIn(text)) {
            return ParseOutcome.ProhibitedOrUnsupported(
                message = "Deleting contacts or data is not supported in ElderAssist for your safety."
            )
        }

        if (Regex("""\b(pay|payment|upi|gpay|phonepe|paytm|bank|banking|transfer money|send money)\b""")
                .containsMatchIn(text)
        ) {
            return ParseOutcome.ProhibitedOrUnsupported(
                message = "Banking and payment actions are blocked in ElderAssist for your safety."
            )
        }

        if (Regex("""\b(transfer|copy|move)\b.*\b(data|phone|everything)\b""").containsMatchIn(text)) {
            return ParseOutcome.ProhibitedOrUnsupported(
                message = "Full phone data transfer is not supported yet. I can open general Settings if you'd like.",
                fallbackDestination = SettingDestination.GENERAL_SETTINGS
            )
        }

        if (Regex("""\b(send (?:a )?(?:message|text|sms))\b""").containsMatchIn(text)) {
            return ParseOutcome.ProhibitedOrUnsupported(
                message = "Sending messages directly is not supported yet. Try 'Open Messages' or 'Open WhatsApp'."
            )
        }

        return null
    }

    private fun checkAmbiguous(text: String): ParseOutcome.Ambiguous? {
        val connectionPhrases = setOf(
            "turn on the connection",
            "turn on connection",
            "enable connection",
            "open connection",
            "open the connection",
            "connect",
            "turn off the connection",
            "turn off connection"
        )
        if (text in connectionPhrases) {
            return ParseOutcome.Ambiguous(
                question = "Do you mean Wi-Fi or Bluetooth?",
                options = listOf(
                    ClarificationOption("Open Wi-Fi settings", "open wifi settings"),
                    ClarificationOption("Open Bluetooth settings", "open bluetooth settings")
                )
            )
        }
        return null
    }

    private fun parseSystemNavigation(text: String): ParseOutcome.Matched? {
        val stripped = stripPolitePrefix(text)
        return when {
            text == "go home" ||
                text == "home" ||
                text == "home screen" ||
                text.contains("home screen") ||
                text.contains("take me home") ||
                text.contains("ghar screen") ||
                stripped == "home" ||
                stripped == "go home" -> ParseOutcome.Matched(
                action = AssistantAction.GoHome,
                summaryForUser = "Going to the home screen."
            )

            text == "go back" ||
                text == "back" ||
                text.contains("previous screen") ||
                text.contains("return to previous") ||
                stripped == "go back" ||
                stripped == "back" -> ParseOutcome.Matched(
                action = AssistantAction.GoBack,
                summaryForUser = "Going back to the previous screen."
            )

            text.contains("recent apps") ||
                text.contains("recent applications") ||
                text == "recents" ||
                text == "show recents" ||
                stripped == "recents" -> ParseOutcome.Matched(
                action = AssistantAction.OpenRecents,
                summaryForUser = "Opening your recent apps."
            )

            else -> null
        }
    }

    private fun parseContactsCommand(text: String): ParseOutcome? {
        // Rename contact: "rename Rahul to Rahul Sharma" / "change Rahul name to Rahul Sharma"
        val renameMatch = Regex(
            """^(?:please |can you |could you )?(?:rename|change)(?: contact)? (.+?) (?:name )?to (.+)$"""
        ).find(text)
        if (renameMatch != null) {
            val currentName = renameMatch.groupValues[1]
                .removePrefix("this contact")
                .trim()
                .toTitleCase()
            val newName = renameMatch.groupValues[2].trim().toTitleCase()
            if (currentName.isNotEmpty() && newName.isNotEmpty() &&
                !currentName.contains("text") && !currentName.contains("font") &&
                !currentName.contains("brightness") && !currentName.contains("volume")
            ) {
                return ParseOutcome.Matched(
                    action = AssistantAction.RenameContact(
                        currentDisplayName = currentName,
                        newDisplayName = newName
                    ),
                    summaryForUser = "Preparing to rename $currentName to $newName."
                )
            }
        }

        // Create contact: "save number 9876543210 as Rahul" or "add Rahul phone number 9876543210"
        val saveAsMatch = Regex(
            """^(?:please |can you )?(?:add|save|create)(?: contact| this number| number)? ([+\d][\d\s\-]{5,}) (?:as|for|named) ([a-zA-Z][a-zA-Z\s]{0,40})$"""
        ).find(text)
        if (saveAsMatch != null) {
            val phone = saveAsMatch.groupValues[1].replace(Regex("""\s+"""), "")
            val name = saveAsMatch.groupValues[2].trim().toTitleCase()
            return ParseOutcome.Matched(
                action = AssistantAction.CreateContact(displayName = name, phoneNumber = phone),
                summaryForUser = "Preparing to save contact $name."
            )
        }

        val addNameNumberMatch = Regex(
            """^(?:please |can you )?(?:add|save|create)(?: contact)? ([a-zA-Z][a-zA-Z\s]{0,40}?) (?:with |phone |number |phone number )*([+\d][\d\s\-]{5,})$"""
        ).find(text)
        if (addNameNumberMatch != null) {
            val name = addNameNumberMatch.groupValues[1].trim().toTitleCase()
            val phone = addNameNumberMatch.groupValues[2].replace(Regex("""\s+"""), "")
            return ParseOutcome.Matched(
                action = AssistantAction.CreateContact(displayName = name, phoneNumber = phone),
                summaryForUser = "Preparing to save contact $name."
            )
        }

        // Incomplete create contact -> ask clarification
        val incompleteAdd = Regex(
            """^(?:please |can you )?(?:add|save|create)(?: new)? contact(?: named| for)? ?([a-zA-Z\s]*)$"""
        ).find(text)
        if (incompleteAdd != null) {
            val possibleName = incompleteAdd.groupValues[1].trim().toTitleCase()
            val question = if (possibleName.isNotEmpty()) {
                "What phone number should I save for $possibleName?"
            } else {
                "Please tell me the contact name and phone number, for example: Add Rahul 9876543210."
            }
            return ParseOutcome.Ambiguous(question = question, options = emptyList())
        }

        // Open contacts: "open contacts", "show my phonebook"
        if (text == "contacts" ||
            text == "open contacts" ||
            text == "show contacts" ||
            text.contains("phonebook") ||
            text.contains("phone book") ||
            text.contains("open my contacts") ||
            text.contains("show my contacts")
        ) {
            return ParseOutcome.Matched(
                action = AssistantAction.OpenApp(SupportedApp.CONTACTS),
                summaryForUser = "Opening Contacts."
            )
        }

        // Search contact: "find Rahul", "search for Mom", "look up Rahul"
        val searchMatch = Regex(
            """^(?:please |can you |could you )?(?:find|search(?: for)?|look up|locate)(?: contact)? ([a-zA-Z][a-zA-Z\s]{0,40}?)(?: in(?: my)? contacts)?$"""
        ).find(text)
        if (searchMatch != null) {
            val query = searchMatch.groupValues[1].trim().toTitleCase()
            if (query.isNotEmpty() && !isSettingKeyword(query.lowercase())) {
                return ParseOutcome.Matched(
                    action = AssistantAction.SearchContact(query),
                    summaryForUser = "Searching your contacts for $query."
                )
            }
        }

        // WhatsApp Call / Video Call: "whatsapp call Mom", "whatsapp video call Mom", "video call Rahul"
        val waCallMatch = Regex(
            """^(?:please |can you |could you )?(?:whatsapp (?:video )?call|video call on whatsapp|call on whatsapp|video call)(?: to)? ([a-zA-Z0-9+\s\-]{2,40})$"""
        ).find(text)
        if (waCallMatch != null) {
            val target = waCallMatch.groupValues[1].trim()
            val isVideo = text.contains("video")
            val callType = if (isVideo) "WhatsApp video call" else "WhatsApp call"
            if (target.isNotEmpty() && !isSettingKeyword(target.lowercase())) {
                return ParseOutcome.Matched(
                    action = AssistantAction.PlaceWhatsAppCall(target, isVideo = isVideo),
                    summaryForUser = "Starting $callType with $target."
                )
            }
        }

        // Call contact or number: "call Rahul", "call Mom", "call 9876543210", "make a call to John"
        val callMatch = Regex(
            """^(?:please |can you |could you )?(?:call|phone|dial|make a call to) ([a-zA-Z0-9+\s\-]{2,40})$"""
        ).find(text)
        if (callMatch != null) {
            val target = callMatch.groupValues[1].trim()
            if (target.isNotEmpty() && !isSettingKeyword(target.lowercase())) {
                return ParseOutcome.Matched(
                    action = AssistantAction.PlaceCall(target),
                    summaryForUser = "Calling $target."
                )
            }
        }

        return null
    }

    private fun parseSettingsCommand(text: String): ParseOutcome.Matched? {
        return when {
            text.contains("mobile data") ||
                text.contains("cellular data") ||
                text.contains("data setting") ||
                text.contains("data usage") ||
                text.contains("cellular") ||
                text.contains("sim card") ||
                text.contains("mobile network") -> ParseOutcome.Matched(
                action = AssistantAction.OpenSetting(SettingDestination.MOBILE_DATA),
                summaryForUser = "Opening mobile data settings."
            )

            text.contains("airplane mode") ||
                text.contains("aeroplane mode") ||
                text.contains("flight mode") ||
                text == "airplane" ||
                text == "aeroplane" ||
                text.contains("turn on airplane") ||
                text.contains("open airplane") -> ParseOutcome.Matched(
                action = AssistantAction.OpenSetting(SettingDestination.AIRPLANE_MODE),
                summaryForUser = "Opening airplane mode settings."
            )

            text.contains("wifi") ||
                text.contains("wi-fi") ||
                text.contains("wi fi") ||
                text.contains("wireless") ||
                text.contains("connect to internet") ||
                text.contains("connect to wifi") ||
                text.contains("hotel wifi") ||
                text.contains("wireless network") -> ParseOutcome.Matched(
                action = AssistantAction.OpenSetting(SettingDestination.WIFI),
                summaryForUser = "Opening Wi-Fi settings."
            )

            text.contains("bluetooth") ||
                text.contains("blue tooth") ||
                text.contains("headphone") ||
                text.contains("hearing aid") ||
                text.contains("earbud") -> ParseOutcome.Matched(
                action = AssistantAction.OpenSetting(SettingDestination.BLUETOOTH),
                summaryForUser = "Opening Bluetooth settings."
            )

            text.contains("hotspot") ||
                text.contains("hot spot") ||
                text.contains("tether") ||
                text.contains("share my internet") ||
                text.contains("connect another phone to my internet") -> ParseOutcome.Matched(
                action = AssistantAction.OpenSetting(SettingDestination.HOTSPOT),
                summaryForUser = "Opening hotspot and tethering settings."
            )

            text.contains("accessibility") ||
                text.contains("talkback") ||
                text.contains("screen reader") ||
                text.contains("magnifier") ||
                text.contains("make text bigger") ||
                text.contains("make the text bigger") ||
                text.contains("make writing bigger") ||
                text.contains("make the writing bigger") ||
                text.contains("larger text") -> ParseOutcome.Matched(
                action = AssistantAction.OpenSetting(SettingDestination.ACCESSIBILITY),
                summaryForUser = "Opening Accessibility settings."
            )

            text.contains("display") ||
                text.contains("brightness") ||
                text.contains("text size") ||
                text.contains("font size") ||
                text.contains("screen size") ||
                text.contains("dark mode") ||
                text.contains("cant see") ||
                text.contains("can't see") ||
                text.contains("cannot see") ||
                text.contains("screen is too") ||
                text.contains("screen too") ||
                text.contains("unable to read") ||
                text.contains("read the text") ||
                text.contains("make screen") ||
                text.contains("hurt my eyes") ||
                text.contains("hurting my eyes") -> ParseOutcome.Matched(
                action = AssistantAction.OpenSetting(SettingDestination.DISPLAY),
                summaryForUser = "Opening Display settings to adjust screen brightness and readability."
            )

            text.contains("sound") ||
                text.contains("volume") ||
                text.contains("ringtone") ||
                text.contains("ringer") ||
                text.contains("loudness") ||
                text.contains("cant hear") ||
                text.contains("can't hear") ||
                text.contains("cannot hear") ||
                text.contains("too quiet") ||
                text.contains("too soft") -> ParseOutcome.Matched(
                action = AssistantAction.OpenSetting(SettingDestination.SOUND),
                summaryForUser = "Opening Sound settings to adjust volume."
            )

            text.contains("battery") ||
                text.contains("power saver") ||
                text.contains("phone is dying") ||
                text.contains("save battery") ||
                text.contains("low battery") -> ParseOutcome.Matched(
                action = AssistantAction.OpenSetting(SettingDestination.BATTERY),
                summaryForUser = "Opening Battery settings."
            )

            text == "settings" ||
                text == "open settings" ||
                text == "show settings" ||
                text.contains("phone settings") ||
                text.contains("device settings") -> ParseOutcome.Matched(
                action = AssistantAction.OpenSetting(SettingDestination.GENERAL_SETTINGS),
                summaryForUser = "Opening phone settings."
            )

            else -> null
        }
    }

    private fun parseAppCommand(text: String): ParseOutcome.Matched? {
        val normalized = text
            .replace("what's app", "whatsapp")
            .replace("whats app", "whatsapp")
            .replace("what app", "whatsapp")
            .replace("you tube", "youtube")
            .replace("u tube", "youtube")
            .replace("call logs", "call log")
            .replace("call history", "call log")
            .trim()

        // 1. Direct standalone app mentions: "whatsapp", "youtube", "camera", etc.
        val directApps = mapOf(
            "whatsapp" to "WhatsApp",
            "youtube" to "YouTube",
            "messages" to "Messages",
            "message" to "Messages",
            "sms" to "Messages",
            "call log" to "Call Log",
            "camera" to "Camera",
            "gallery" to "Photos",
            "photos" to "Photos",
            "chrome" to "Chrome",
            "browser" to "Browser",
            "play store" to "Play Store",
            "calculator" to "Calculator"
        )
        if (directApps.containsKey(normalized)) {
            val displayName = directApps[normalized]!!
            return ParseOutcome.Matched(
                action = AssistantAction.OpenNamedApp(normalized),
                summaryForUser = "Opening $displayName."
            )
        }

        // 2. Phrased app requests: "open whatsapp", "can you launch youtube please", "open the camera"
        val openAppMatch = Regex(
            """^(?:please |can you |could you |kindly |help me )?(?:open|launch|start|show|go to)(?: the| my)? ([a-zA-Z0-9\s]{2,30}?)(?: for me| please)?$"""
        ).find(normalized)
        if (openAppMatch != null) {
            val appCandidate = openAppMatch.groupValues[1].trim()
            if (!isSettingKeyword(appCandidate) &&
                appCandidate != "settings" &&
                appCandidate != "home" &&
                appCandidate != "recents" &&
                appCandidate != "back"
            ) {
                if (appCandidate == "contacts" || appCandidate == "phonebook") {
                    return ParseOutcome.Matched(
                        action = AssistantAction.OpenApp(SupportedApp.CONTACTS),
                        summaryForUser = "Opening Contacts."
                    )
                }
                val prettyName = directApps[appCandidate] ?: appCandidate.toTitleCase()
                return ParseOutcome.Matched(
                    action = AssistantAction.OpenNamedApp(appCandidate),
                    summaryForUser = "Opening $prettyName."
                )
            }
        }
        return null
    }

    private fun isSettingKeyword(word: String): Boolean {
        val keywords = setOf(
            "wifi", "wi-fi", "bluetooth", "hotspot", "tethering",
            "mobile data", "data", "cellular", "airplane", "aeroplane", "flight",
            "display", "brightness", "sound", "volume", "accessibility",
            "battery", "settings"
        )
        return keywords.any { word.contains(it) }
    }

    private fun String.toTitleCase(): String {
        return split(" ")
            .filter { it.isNotBlank() }
            .joinToString(" ") { part ->
                part.replaceFirstChar { ch ->
                    if (ch.isLowerCase()) ch.titlecase() else ch.toString()
                }
            }
    }
}
