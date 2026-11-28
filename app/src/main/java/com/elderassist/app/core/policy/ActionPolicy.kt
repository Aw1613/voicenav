package com.elderassist.app.core.policy

import com.elderassist.app.core.model.AssistantAction
import com.elderassist.app.core.model.RiskLevel
import com.elderassist.app.core.model.SettingDestination

object ActionPolicy {
    const val MAX_AI_NAVIGATION_STEPS = 5
    const val MIN_AI_INTENT_CONFIDENCE = 0.70
    const val MIN_AI_NAVIGATION_CONFIDENCE = 0.85
    const val MAX_SNAPSHOT_AGE_MS = 15_000L

    val allowedSettings: Set<SettingDestination> = SettingDestination.entries.toSet()

    val excludedSensitivePackages: Set<String> = setOf(
        "com.android.packageinstaller",
        "com.google.android.packageinstaller",
        "com.android.permissioncontroller",
        "com.google.android.permissioncontroller",
        "com.android.keyguard",
        "com.android.biometric",
        "com.google.android.apps.walletnfcrel",
        "com.phonepe.app",
        "com.google.android.apps.nbu.paisa.user",
        "net.one97.paytm"
    )

    val sensitivePackageKeywords: List<String> = listOf(
        "bank",
        "wallet",
        "pay",
        "upi",
        "auth",
        "biometric",
        "keyguard",
        "lockscreen",
        "password",
        "credential"
    )

    fun isPackageExcluded(packageName: String?): Boolean {
        if (packageName.isNullOrBlank()) return false
        val lower = packageName.lowercase()
        if (lower in excludedSensitivePackages) return true
        return sensitivePackageKeywords.any { keyword -> lower.contains(keyword) }
    }

    fun riskLevelFor(action: AssistantAction): RiskLevel = when (action) {
        is AssistantAction.OpenSetting,
        is AssistantAction.OpenApp,
        is AssistantAction.OpenNamedApp,
        is AssistantAction.PlaceCall,
        AssistantAction.GoHome,
        AssistantAction.GoBack,
        AssistantAction.OpenRecents,
        AssistantAction.Cancel,
        is AssistantAction.AskClarification,
        is AssistantAction.Explain,
        is AssistantAction.Unsupported -> RiskLevel.LOW

        is AssistantAction.ClickNode,
        is AssistantAction.Scroll,
        is AssistantAction.SetNodeText -> RiskLevel.MEDIUM

        is AssistantAction.SearchContact -> RiskLevel.HIGH_PRIVACY

        is AssistantAction.CreateContact,
        is AssistantAction.RenameContact -> RiskLevel.CONSEQUENTIAL_WRITE
    }

    fun requiresConfirmation(action: AssistantAction): Boolean = when (action) {
        is AssistantAction.CreateContact,
        is AssistantAction.RenameContact -> true
        else -> false
    }

    fun maskPhoneLast4(phoneNumber: String): String {
        val digits = phoneNumber.filter { it.isDigit() }
        return if (digits.length >= 4) {
            "ending in ${digits.takeLast(4)}"
        } else if (digits.isNotEmpty()) {
            "ending in $digits"
        } else {
            "(no digits)"
        }
    }
}
