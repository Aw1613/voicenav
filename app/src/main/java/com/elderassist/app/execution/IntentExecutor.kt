package com.elderassist.app.execution

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.provider.ContactsContract
import android.provider.Settings
import com.elderassist.app.core.model.SettingDestination

data class IntentLaunchOutcome(
    val launched: Boolean,
    val usedFallback: Boolean,
    val resolvedPackage: String?,
    val errorMessage: String? = null
)

class IntentExecutor(private val context: Context) {

    fun openSetting(destination: SettingDestination): IntentLaunchOutcome {
        val candidateActions = when (destination) {
            SettingDestination.WIFI -> listOf(
                Settings.ACTION_WIFI_SETTINGS,
                Settings.ACTION_WIRELESS_SETTINGS,
                Settings.ACTION_SETTINGS
            )
            SettingDestination.BLUETOOTH -> listOf(
                Settings.ACTION_BLUETOOTH_SETTINGS,
                Settings.ACTION_SETTINGS
            )
            SettingDestination.HOTSPOT -> listOf(
                // No universal public direct hotspot action across all OEMs; prefer safe documented routes.
                Settings.ACTION_WIRELESS_SETTINGS,
                Settings.ACTION_WIFI_SETTINGS,
                Settings.ACTION_SETTINGS
            )
            SettingDestination.DISPLAY -> listOf(
                Settings.ACTION_DISPLAY_SETTINGS,
                Settings.ACTION_SETTINGS
            )
            SettingDestination.SOUND -> listOf(
                Settings.ACTION_SOUND_SETTINGS,
                Settings.ACTION_SETTINGS
            )
            SettingDestination.ACCESSIBILITY -> listOf(
                Settings.ACTION_ACCESSIBILITY_SETTINGS,
                Settings.ACTION_SETTINGS
            )
            SettingDestination.BATTERY -> listOf(
                Settings.ACTION_BATTERY_SAVER_SETTINGS,
                Settings.ACTION_SETTINGS
            )
            SettingDestination.GENERAL_SETTINGS -> listOf(
                Settings.ACTION_SETTINGS
            )
        }

        candidateActions.forEachIndexed { index, action ->
            val intent = Intent(action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val resolved = intent.resolveActivity(context.packageManager)
            if (resolved != null) {
                return try {
                    context.startActivity(intent)
                    IntentLaunchOutcome(
                        launched = true,
                        usedFallback = index > 0,
                        resolvedPackage = resolved.packageName
                    )
                } catch (_: ActivityNotFoundException) {
                    // Try next fallback
                    IntentLaunchOutcome(false, index > 0, null)
                } catch (_: SecurityException) {
                    IntentLaunchOutcome(false, index > 0, null)
                }
            }
        }

        return IntentLaunchOutcome(
            launched = false,
            usedFallback = false,
            resolvedPackage = null,
            errorMessage = "I couldn't find ${destination.displayName} on this phone."
        )
    }

    fun openAccessibilitySettings(): IntentLaunchOutcome {
        return openSetting(SettingDestination.ACCESSIBILITY)
    }

    fun openAppDetailsSettings(): IntentLaunchOutcome {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = android.net.Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return launchIntent(intent, "Could not open app settings.")
    }

    fun openContactsApp(): IntentLaunchOutcome {
        val selector = Intent.makeMainSelectorActivity(
            Intent.ACTION_MAIN,
            Intent.CATEGORY_APP_CONTACTS
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val firstTry = launchIntent(selector, "Could not open Contacts.")
        if (firstTry.launched) return firstTry

        val viewIntent = Intent(Intent.ACTION_VIEW, ContactsContract.Contacts.CONTENT_URI)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return launchIntent(viewIntent, "I couldn't find a Contacts app on this phone.")
    }

    fun launchInsertContact(displayName: String, phoneNumber: String): IntentLaunchOutcome {
        val intent = Intent(ContactsContract.Intents.Insert.ACTION).apply {
            type = ContactsContract.RawContacts.CONTENT_TYPE
            putExtra(ContactsContract.Intents.Insert.NAME, displayName)
            putExtra(ContactsContract.Intents.Insert.PHONE, phoneNumber)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return launchIntent(intent, "Could not open the add-contact screen on this phone.")
    }

    fun launchEditContact(
        contactId: Long,
        lookupKey: String,
        newDisplayName: String
    ): IntentLaunchOutcome {
        val contactUri = ContactsContract.Contacts.getLookupUri(contactId, lookupKey)
            ?: return IntentLaunchOutcome(
                launched = false,
                usedFallback = false,
                resolvedPackage = null,
                errorMessage = "Could not locate that contact to edit."
            )

        val intent = Intent(Intent.ACTION_EDIT).apply {
            setDataAndType(contactUri, ContactsContract.Contacts.CONTENT_ITEM_TYPE)
            putExtra(ContactsContract.Intents.Insert.NAME, newDisplayName)
            putExtra("finishActivityOnSaveCompleted", true)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return launchIntent(intent, "Could not open the contact editor on this phone.")
    }

    fun openAppByName(appName: String): IntentLaunchOutcome {
        val cleanName = appName.trim().lowercase()

        // 1. Direct package / Category shortcuts
        val intentToTry = when {
            cleanName == "whatsapp" || cleanName.contains("whatsapp") -> {
                context.packageManager.getLaunchIntentForPackage("com.whatsapp")
                    ?: context.packageManager.getLaunchIntentForPackage("com.whatsapp.w4b")
            }
            cleanName.contains("message") || cleanName.contains("sms") || cleanName.contains("text") -> {
                Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_MESSAGING)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            cleanName.contains("call log") || cleanName.contains("call history") || cleanName.contains("dialer") || cleanName == "phone" -> {
                Intent(Intent.ACTION_VIEW, android.provider.CallLog.Calls.CONTENT_URI)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            cleanName == "camera" || cleanName.contains("camera") -> {
                Intent("android.media.action.STILL_IMAGE_CAMERA")
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            cleanName == "gallery" || cleanName.contains("photo") -> {
                Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_GALLERY)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            cleanName.contains("browser") || cleanName == "chrome" || cleanName.contains("internet") -> {
                Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_BROWSER)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            cleanName.contains("youtube") -> {
                context.packageManager.getLaunchIntentForPackage("com.google.android.youtube")
            }
            cleanName.contains("contact") -> {
                return openContactsApp()
            }
            cleanName.contains("setting") -> {
                return openSetting(SettingDestination.GENERAL_SETTINGS)
            }
            else -> null
        }

        if (intentToTry != null) {
            val outcome = launchIntent(intentToTry, "Could not open $appName.")
            if (outcome.launched) return outcome
        }

        // 2. Search installed applications by display label
        try {
            val pm = context.packageManager
            val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
            val activities = pm.queryIntentActivities(launcherIntent, 0)

            val exactMatch = activities.firstOrNull {
                it.loadLabel(pm).toString().trim().equals(cleanName, ignoreCase = true)
            }
            val targetActivity = exactMatch ?: activities.firstOrNull {
                it.loadLabel(pm).toString().trim().lowercase().contains(cleanName) ||
                    it.activityInfo.packageName.lowercase().contains(cleanName)
            }

            if (targetActivity != null) {
                val launchIntent = pm.getLaunchIntentForPackage(targetActivity.activityInfo.packageName)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    return launchIntent(launchIntent, "Could not launch ${targetActivity.loadLabel(pm)}.")
                }
            }
        } catch (_: Exception) {}

        return IntentLaunchOutcome(
            launched = false,
            usedFallback = false,
            resolvedPackage = null,
            errorMessage = "I couldn't find an app named '$appName' installed on this device."
        )
    }

    fun placeCall(recipientNumberOrQuery: String): IntentLaunchOutcome {
        val clean = recipientNumberOrQuery.trim()
        val digits = clean.filter { it.isDigit() || it == '+' }
        val uri = if (digits.length >= 3) {
            android.net.Uri.parse("tel:$digits")
        } else {
            android.net.Uri.parse("tel:${android.net.Uri.encode(clean)}")
        }
        val dialIntent = Intent(Intent.ACTION_DIAL, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val outcome = launchIntent(dialIntent, "Could not open phone dialer.")
        if (outcome.launched) return outcome

        // Fallback: generic dialer intent
        val genericDialer = Intent(Intent.ACTION_DIAL).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return launchIntent(genericDialer, "Could not open phone dialer.")
    }

    private fun launchIntent(intent: Intent, failureMessage: String): IntentLaunchOutcome {
        val resolved = intent.resolveActivity(context.packageManager)
            ?: return IntentLaunchOutcome(false, false, null, failureMessage)
        return try {
            context.startActivity(intent)
            IntentLaunchOutcome(
                launched = true,
                usedFallback = false,
                resolvedPackage = resolved.packageName
            )
        } catch (_: ActivityNotFoundException) {
            IntentLaunchOutcome(false, false, null, failureMessage)
        } catch (_: SecurityException) {
            IntentLaunchOutcome(false, false, null, failureMessage)
        }
    }
}
