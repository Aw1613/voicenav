package com.elderassist.app.accessibility

import android.view.accessibility.AccessibilityNodeInfo

/**
 * Immutable path locator for finding a node inside a fresh rootInActiveWindow tree
 * without retaining stale AccessibilityNodeInfo references across UI transitions.
 */
data class NodeLocator(
    val snapshotId: String,
    val childIndexPath: List<Int>,
    val expectedClassName: String?,
    val expectedText: String?,
    val expectedContentDescription: String?,
    val expectedPackageName: String?
) {
    fun resolve(root: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        var current: AccessibilityNodeInfo = root ?: return null
        if (!expectedPackageName.isNullOrBlank() &&
            current.packageName?.toString() != expectedPackageName
        ) {
            return null
        }
        for (index in childIndexPath) {
            if (index < 0 || index >= current.childCount) {
                return null
            }
            current = current.getChild(index) ?: return null
        }
        val matchesClass = expectedClassName == null ||
            current.className?.toString() == expectedClassName
        val matchesText = expectedText == null ||
            current.text?.toString() == expectedText
        val matchesDesc = expectedContentDescription == null ||
            current.contentDescription?.toString() == expectedContentDescription

        return if (matchesClass && matchesText && matchesDesc) current else null
    }
}
