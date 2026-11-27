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
