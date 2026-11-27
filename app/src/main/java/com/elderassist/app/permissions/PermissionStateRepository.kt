package com.elderassist.app.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.core.content.ContextCompat
import com.elderassist.app.accessibility.AccessibilityServiceState
import com.elderassist.app.core.model.Capability
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class CapabilitySnapshot(
    val microphoneGranted: Boolean = false,
    val contactsReadGranted: Boolean = false,
    val accessibilityEnabled: Boolean = false,
    val networkAvailable: Boolean = false,
    val geminiKeyConfigured: Boolean = false
) {
    val readyCount: Int
        get() = listOf(
            microphoneGranted,
            contactsReadGranted,
            accessibilityEnabled,
            networkAvailable && geminiKeyConfigured
        ).count { it }

    fun missingCapabilities(): Set<Capability> = buildSet {
        if (!microphoneGranted) add(Capability.MICROPHONE)
        if (!contactsReadGranted) add(Capability.CONTACTS_READ)
        if (!accessibilityEnabled) add(Capability.ACCESSIBILITY)
        if (!networkAvailable || !geminiKeyConfigured) add(Capability.INTERNET_AI)
    }
}

class PermissionStateRepository(
    private val context: Context,
    private val isGeminiConfigured: () -> Boolean
) {
    private val _state = MutableStateFlow(CapabilitySnapshot())
    val state: StateFlow<CapabilitySnapshot> = _state.asStateFlow()

    fun refresh(): CapabilitySnapshot {
        val mic = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        val contacts = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED

        val accessibility = AccessibilityServiceState.isAvailable(context)
        val network = checkNetworkAvailable()
        val snapshot = CapabilitySnapshot(
            microphoneGranted = mic,
            contactsReadGranted = contacts,
            accessibilityEnabled = accessibility,
            networkAvailable = network,
            geminiKeyConfigured = isGeminiConfigured()
        )
        _state.value = snapshot
        return snapshot
    }

    private fun checkNetworkAvailable(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val activeNetwork = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(activeNetwork) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
