package com.elderassist.app.feature.settings

import com.elderassist.app.core.model.ActionExecutionResult
import com.elderassist.app.core.model.SettingDestination
import com.elderassist.app.execution.IntentExecutor

class SettingsActionHandler(
    private val intentExecutor: IntentExecutor
) {
    fun open(destination: SettingDestination): ActionExecutionResult {
        val outcome = intentExecutor.openSetting(destination)
        if (!outcome.launched) {
            return ActionExecutionResult.Failed(
                userMessage = outcome.errorMessage
                    ?: "I couldn't find ${destination.displayName} on this phone."
            )
        }

        // Honest feedback: never claim a toggle changed when we only opened a Settings screen.
        val message = when (destination) {
            SettingDestination.WIFI ->
                "I've opened Wi-Fi settings. You can turn Wi-Fi on or choose a network here."
            SettingDestination.BLUETOOTH ->
                "I've opened Bluetooth settings. You can turn Bluetooth on or off here."
            SettingDestination.HOTSPOT -> if (outcome.usedFallback) {
                "I've opened Settings. Look for Network & internet or Hotspot & tethering to turn on your hotspot."
            } else {
                "I've opened network and wireless settings. Look for Hotspot & tethering here."
            }
            SettingDestination.DISPLAY ->
                "I've opened Display settings. You can adjust brightness or text size here."
            SettingDestination.SOUND ->
                "I've opened Sound settings. You can adjust volume or ringtones here."
            SettingDestination.ACCESSIBILITY ->
                "I've opened Accessibility settings. You can adjust text size, magnification, or ElderAssist access here."
            SettingDestination.BATTERY ->
                "I've opened Battery settings. You can check battery usage or Battery Saver here."
            SettingDestination.GENERAL_SETTINGS ->
                "I've opened Phone settings."
        }

        return ActionExecutionResult.Executed(
            message = message,
            openedPackage = outcome.resolvedPackage,
            usedFallback = outcome.usedFallback
        )
    }
}
