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
            SettingDestination.MOBILE_DATA -> listOf(
                Settings.ACTION_DATA_ROAMING_SETTINGS,
                Settings.ACTION_NETWORK_OPERATOR_SETTINGS,
                Settings.ACTION_DATA_USAGE_SETTINGS,
                Settings.ACTION_WIRELESS_SETTINGS,
                Settings.ACTION_SETTINGS
            )
            SettingDestination.AIRPLANE_MODE -> listOf(
                Settings.ACTION_AIRPLANE_MODE_SETTINGS,
                Settings.ACTION_WIRELESS_SETTINGS,
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
        val normalized = cleanName
            .replace("what's app", "whatsapp")
            .replace("whats app", "whatsapp")
            .replace("what app", "whatsapp")
            .replace("you tube", "youtube")
            .replace("u tube", "youtube")
            .replace("playstore", "play store")
            .replace("call logs", "call log")
            .replace("call history", "call log")
            .trim()

        // 1. WhatsApp
        if (normalized.contains("whatsapp")) {
            val waLaunch = context.packageManager.getLaunchIntentForPackage("com.whatsapp")
                ?: context.packageManager.getLaunchIntentForPackage("com.whatsapp.w4b")
            if (waLaunch != null) {
                val res = launchIntent(waLaunch, "Could not open WhatsApp.")
                if (res.launched) return res
            }
            // Direct action fallback
            val waIntent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("whatsapp://send"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val res2 = launchIntent(waIntent, "Could not open WhatsApp.")
            if (res2.launched) return res2
        }

        // 2. YouTube
        if (normalized.contains("youtube")) {
            val ytLaunch = context.packageManager.getLaunchIntentForPackage("com.google.android.youtube")
            if (ytLaunch != null) {
                val res = launchIntent(ytLaunch, "Could not open YouTube.")
                if (res.launched) return res
            }
            val ytIntent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://www.youtube.com"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val res2 = launchIntent(ytIntent, "Could not open YouTube.")
            if (res2.launched) return res2
        }

        // 3. Call Log / Dialer / Phone
        if (normalized.contains("call log") || normalized == "call history" || normalized == "dialer" || normalized == "phone") {
            val callLogIntent = Intent(Intent.ACTION_VIEW, android.provider.CallLog.Calls.CONTENT_URI)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val res = launchIntent(callLogIntent, "Could not open Call Log.")
            if (res.launched) return res

            val dialerIntent = Intent(Intent.ACTION_DIAL).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val res2 = launchIntent(dialerIntent, "Could not open Phone.")
            if (res2.launched) return res2
        }

        // 4. Messages / SMS
        if (normalized.contains("message") || normalized.contains("sms") || normalized.contains("text")) {
            val msgSelector = Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_MESSAGING)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val res = launchIntent(msgSelector, "Could not open Messages.")
            if (res.launched) return res

            val msgPkg = context.packageManager.getLaunchIntentForPackage("com.google.android.apps.messaging")
                ?: context.packageManager.getLaunchIntentForPackage("com.samsung.android.messaging")
            if (msgPkg != null) {
                val res2 = launchIntent(msgPkg, "Could not open Messages.")
                if (res2.launched) return res2
            }

            val smsIntent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("sms:"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val res3 = launchIntent(smsIntent, "Could not open Messages.")
            if (res3.launched) return res3
        }

        // 5. Camera
        if (normalized.contains("camera")) {
            val camIntent = Intent("android.media.action.STILL_IMAGE_CAMERA")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val res = launchIntent(camIntent, "Could not open Camera.")
            if (res.launched) return res

            val camCapture = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val res2 = launchIntent(camCapture, "Could not open Camera.")
            if (res2.launched) return res2
        }

        // 6. Gallery / Photos
        if (normalized.contains("gallery") || normalized.contains("photo")) {
            val galSelector = Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_GALLERY)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val res = launchIntent(galSelector, "Could not open Photos.")
            if (res.launched) return res

            val photosLaunch = context.packageManager.getLaunchIntentForPackage("com.google.android.apps.photos")
            if (photosLaunch != null) {
                val res2 = launchIntent(photosLaunch, "Could not open Photos.")
                if (res2.launched) return res2
            }
        }

        // 7. Chrome / Browser
        if (normalized.contains("browser") || normalized.contains("chrome") || normalized.contains("internet")) {
            val browserSelector = Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_BROWSER)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val res = launchIntent(browserSelector, "Could not open Browser.")
            if (res.launched) return res

            val chromeLaunch = context.packageManager.getLaunchIntentForPackage("com.android.chrome")
            if (chromeLaunch != null) {
                val res2 = launchIntent(chromeLaunch, "Could not open Chrome.")
                if (res2.launched) return res2
            }
        }

        // 8. Play Store
        if (normalized.contains("play store") || normalized == "store") {
            val storeLaunch = context.packageManager.getLaunchIntentForPackage("com.android.vending")
            if (storeLaunch != null) {
                val res = launchIntent(storeLaunch, "Could not open Google Play Store.")
                if (res.launched) return res
            }
        }

        // 9. Contacts & Settings
        if (normalized.contains("contact") || normalized == "phonebook") {
            return openContactsApp()
        }
        if (normalized.contains("setting")) {
            return openSetting(SettingDestination.GENERAL_SETTINGS)
        }

        // 10. Search all installed applications by display label or package
        try {
            val pm = context.packageManager
            val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
            val activities = pm.queryIntentActivities(launcherIntent, 0)

            val exactMatch = activities.firstOrNull {
                it.loadLabel(pm).toString().trim().equals(normalized, ignoreCase = true)
            }
            val partialMatch = exactMatch ?: activities.firstOrNull {
                val label = it.loadLabel(pm).toString().trim().lowercase()
                label.contains(normalized) || normalized.contains(label) ||
                    it.activityInfo.packageName.lowercase().contains(normalized)
            }

            if (partialMatch != null) {
                val launchIntent = pm.getLaunchIntentForPackage(partialMatch.activityInfo.packageName)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    return launchIntent(launchIntent, "Could not launch ${partialMatch.loadLabel(pm)}.")
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

    fun placeCall(recipientNumberOrQuery: String, directCall: Boolean = true): IntentLaunchOutcome {
        val clean = recipientNumberOrQuery.trim()
        val digits = clean.filter { it.isDigit() || it == '+' }
        val uri = if (digits.length >= 3) {
            android.net.Uri.parse("tel:$digits")
        } else {
            android.net.Uri.parse("tel:${android.net.Uri.encode(clean)}")
        }

        // 1. Try direct call if CALL_PHONE permission is granted
        if (directCall && context.checkSelfPermission(android.Manifest.permission.CALL_PHONE) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            try {
                val callIntent = Intent(Intent.ACTION_CALL, uri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                val callRes = launchIntent(callIntent, "Could not place direct call.")
                if (callRes.launched) return callRes
            } catch (_: SecurityException) {}
        }

        // 2. Fallback: ACTION_DIAL with number pre-filled
        val dialIntent = Intent(Intent.ACTION_DIAL, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val outcome = launchIntent(dialIntent, "Could not open phone dialer.")
        if (outcome.launched) return outcome

        // 3. Fallback: generic dialer intent
        val genericDialer = Intent(Intent.ACTION_DIAL).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return launchIntent(genericDialer, "Could not open phone dialer.")
    }

    fun placeWhatsAppCall(
        recipientPhoneOrQuery: String,
        isVideo: Boolean = false,
        contactId: Long? = null
    ): IntentLaunchOutcome {
        val clean = recipientPhoneOrQuery.trim()
        val digits = clean.filter { it.isDigit() || it == '+' }

        // 1. If contactId is provided, query ContactsContract.Data for WhatsApp MIME type
        if (contactId != null && contactId > 0) {
            try {
                val mimeType = if (isVideo) {
                    "vnd.android.cursor.item/vnd.com.whatsapp.video.call"
                } else {
                    "vnd.android.cursor.item/vnd.com.whatsapp.call"
                }
                val projection = arrayOf(android.provider.ContactsContract.Data._ID)
                val selection = "${android.provider.ContactsContract.Data.CONTACT_ID} = ? AND (${android.provider.ContactsContract.Data.MIMETYPE} = ? OR ${android.provider.ContactsContract.Data.MIMETYPE} = ?)"
                val args = arrayOf(
                    contactId.toString(),
                    mimeType,
                    if (isVideo) mimeType else "vnd.android.cursor.item/vnd.com.whatsapp.voip.call"
                )
                context.contentResolver.query(
                    android.provider.ContactsContract.Data.CONTENT_URI,
                    projection,
                    selection,
                    args,
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val dataId = cursor.getLong(0)
                        val waCallIntent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(
                                android.net.Uri.parse("content://com.android.contacts/data/$dataId"),
                                mimeType
                            )
                            setPackage("com.whatsapp")
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        val outcome = launchIntent(waCallIntent, "Could not initiate WhatsApp call.")
                        if (outcome.launched) return outcome
                    }
                }
            } catch (_: Exception) {}
        }

        // 2. Fallback: launch WhatsApp chat/call via URL scheme
        if (digits.length >= 7) {
            val phoneDigits = digits.removePrefix("+")
            try {
                val waUri = android.net.Uri.parse("https://api.whatsapp.com/send?phone=$phoneDigits")
                val waIntent = Intent(Intent.ACTION_VIEW, waUri).apply {
                    setPackage("com.whatsapp")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                val outcome = launchIntent(waIntent, "Could not open WhatsApp.")
                if (outcome.launched) return outcome
            } catch (_: Exception) {}
        }

        // 3. Fallback: launch WhatsApp app directly
        val waLaunch = context.packageManager.getLaunchIntentForPackage("com.whatsapp")
            ?: context.packageManager.getLaunchIntentForPackage("com.whatsapp.w4b")
        if (waLaunch != null) {
            waLaunch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            return launchIntent(waLaunch, "Could not open WhatsApp.")
        }

        return IntentLaunchOutcome(
            launched = false,
            usedFallback = false,
            resolvedPackage = null,
            errorMessage = "WhatsApp is not installed on this device."
        )
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
