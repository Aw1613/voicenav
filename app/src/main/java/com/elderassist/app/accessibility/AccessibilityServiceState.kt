package com.elderassist.app.accessibility

import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import android.text.TextUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.lang.ref.WeakReference

object AccessibilityServiceState {
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    @Volatile
    private var serviceRef: WeakReference<ElderAssistAccessibilityService>? = null

    fun onServiceConnected(service: ElderAssistAccessibilityService) {
        serviceRef = WeakReference(service)
        _isConnected.value = true
    }

    fun onServiceDisconnected() {
        serviceRef = null
        _isConnected.value = false
    }

    fun activeService(): ElderAssistAccessibilityService? = serviceRef?.get()

    fun isServiceEnabledInSystemSettings(context: Context): Boolean {
        val expectedComponent = ComponentName(context, ElderAssistAccessibilityService::class.java)
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val splitter = TextUtils.SimpleStringSplitter(':')
        splitter.setString(enabledServices)
        while (splitter.hasNext()) {
            val componentString = splitter.next()
            val parsed = ComponentName.unflattenFromString(componentString)
            if (parsed == expectedComponent) {
                return true
            }
        }
        return false
    }

    fun isAvailable(context: Context): Boolean {
        return _isConnected.value && activeService() != null &&
            isServiceEnabledInSystemSettings(context)
    }
}
