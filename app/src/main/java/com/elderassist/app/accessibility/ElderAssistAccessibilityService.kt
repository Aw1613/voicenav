package com.elderassist.app.accessibility

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import com.elderassist.app.core.model.ScreenSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ElderAssistAccessibilityService : AccessibilityService() {

    val treeReader = AccessibilityTreeReader()

    private val _lastPackageName = MutableStateFlow<String?>(null)
    val lastPackageName: StateFlow<String?> = _lastPackageName.asStateFlow()

    @Volatile
    var isTaskActive: Boolean = false

    override fun onServiceConnected() {
        super.onServiceConnected()
        AccessibilityServiceState.onServiceConnected(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Privacy requirement: do not inspect or capture unrelated apps when no task is active.
        if (!isTaskActive || event == null) return
        val pkg = event.packageName?.toString()
        if (!pkg.isNullOrBlank()) {
            _lastPackageName.value = pkg
        }
    }

    override fun onInterrupt() {
        isTaskActive = false
        treeReader.clearSnapshot()
    }

    override fun onUnbind(intent: android.content.Intent?): Boolean {
        isTaskActive = false
        treeReader.clearSnapshot()
        AccessibilityServiceState.onServiceDisconnected()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        isTaskActive = false
        treeReader.clearSnapshot()
        AccessibilityServiceState.onServiceDisconnected()
        super.onDestroy()
    }

    fun captureFreshSnapshot(): ScreenSnapshot? {
        val root = rootInActiveWindow ?: return null
        val title = root.window?.title?.toString()
        return treeReader.captureSnapshot(root = root, windowTitle = title)
    }
}
