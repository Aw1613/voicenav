package com.elderassist.app.accessibility

import android.accessibilityservice.AccessibilityService
import android.os.Bundle
import android.view.accessibility.AccessibilityNodeInfo
import com.elderassist.app.core.model.ActionExecutionResult
import com.elderassist.app.core.model.ScrollDirection
import com.elderassist.app.core.policy.ActionPolicy

class AccessibilityExecutor(
    private val fallbackTreeReader: AccessibilityTreeReader
) {
    private fun currentTreeReader(): AccessibilityTreeReader =
        AccessibilityServiceState.activeService()?.treeReader ?: fallbackTreeReader

    fun performGlobalNavigation(globalAction: Int, successMessage: String): ActionExecutionResult {
        val service = AccessibilityServiceState.activeService()
            ?: return ActionExecutionResult.Failed(
                userMessage = "Please enable ElderAssist accessibility access in Android Settings first."
            )

        val ok = service.performGlobalAction(globalAction)
        return if (ok) {
            currentTreeReader().clearSnapshot()
            ActionExecutionResult.Executed(message = successMessage)
        } else {
            ActionExecutionResult.Failed(
                userMessage = "I could not perform that navigation action on this screen."
            )
        }
    }

    fun clickNode(snapshotId: String, nodeId: String, hint: String?): ActionExecutionResult {
        val service = AccessibilityServiceState.activeService()
            ?: return ActionExecutionResult.Failed(
                userMessage = "Please enable ElderAssist accessibility access in Android Settings."
            )

        val root = service.rootInActiveWindow
            ?: return ActionExecutionResult.Failed(
                userMessage = "This screen could not be read reliably, so I stopped."
            )

        if (ActionPolicy.isPackageExcluded(root.packageName?.toString())) {
            return ActionExecutionResult.Failed(
                userMessage = "Stopped because a protected or sensitive screen is open."
            )
        }

        val activeReader = currentTreeReader()
        val locator = activeReader.resolveLocator(snapshotId, nodeId)
            ?: return ActionExecutionResult.Failed(
                userMessage = "The screen changed before I could tap that option, so I stopped."
            )

        val node = locator.resolve(root)
            ?: return ActionExecutionResult.Failed(
                userMessage = "This screen is different from what I expected, so I stopped."
            )

        if (!node.isVisibleToUser || !node.isEnabled) {
            return ActionExecutionResult.Failed(
                userMessage = "That button is not currently available to tap."
            )
        }

        val clicked = performClickOrClickableParent(node)
        return if (clicked) {
            activeReader.clearSnapshot()
            val label = hint ?: node.text?.toString() ?: node.contentDescription?.toString() ?: "item"
            ActionExecutionResult.Executed(
                message = "Selected $label.",
                openedPackage = root.packageName?.toString()
            )
        } else {
            ActionExecutionResult.Failed(
                userMessage = "I found that item, but the screen did not allow tapping it."
            )
        }
    }

    fun scroll(
        snapshotId: String,
        nodeId: String?,
        direction: ScrollDirection
    ): ActionExecutionResult {
        val service = AccessibilityServiceState.activeService()
            ?: return ActionExecutionResult.Failed(
                userMessage = "Please enable ElderAssist accessibility access in Android Settings."
            )

        val root = service.rootInActiveWindow
            ?: return ActionExecutionResult.Failed(
                userMessage = "This screen could not be read reliably, so I stopped."
            )

        val activeReader = currentTreeReader()
        val targetNode = if (nodeId != null) {
            val locator = activeReader.resolveLocator(snapshotId, nodeId)
                ?: return ActionExecutionResult.Failed(
                    userMessage = "The screen changed before scrolling, so I stopped."
                )
            locator.resolve(root)
        } else {
            findFirstScrollable(root)
        }

        if (targetNode == null || !targetNode.isScrollable) {
            return ActionExecutionResult.Failed(
                userMessage = "I could not find a scrollable area on this screen."
            )
        }

        val action = when (direction) {
            ScrollDirection.DOWN, ScrollDirection.RIGHT -> AccessibilityNodeInfo.ACTION_SCROLL_FORWARD
            ScrollDirection.UP, ScrollDirection.LEFT -> AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
        }
        val scrolled = targetNode.performAction(action)
        return if (scrolled) {
            activeReader.clearSnapshot()
            ActionExecutionResult.Executed(
                message = "Scrolled ${direction.name.lowercase()}.",
                openedPackage = root.packageName?.toString()
            )
        } else {
            ActionExecutionResult.Failed("Could not scroll further in that direction.")
        }
    }

    fun setNodeText(snapshotId: String, nodeId: String, text: String): ActionExecutionResult {
        val service = AccessibilityServiceState.activeService()
            ?: return ActionExecutionResult.Failed(
                userMessage = "Please enable ElderAssist accessibility access in Android Settings."
            )

        val root = service.rootInActiveWindow
            ?: return ActionExecutionResult.Failed("Could not read the active screen.")

        val activeReader = currentTreeReader()
        val locator = activeReader.resolveLocator(snapshotId, nodeId)
            ?: return ActionExecutionResult.Failed("Screen changed before typing, so I stopped.")

        val node = locator.resolve(root)
            ?: return ActionExecutionResult.Failed("Input field is no longer on screen.")

        if (!node.isEditable || !node.isEnabled || node.isPassword) {
            return ActionExecutionResult.Failed("That field cannot be safely edited.")
        }

        val args = Bundle().apply {
            putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
        }
        val ok = node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
        return if (ok) {
            activeReader.clearSnapshot()
            ActionExecutionResult.Executed(
                message = "Entered text into the search box.",
                openedPackage = root.packageName?.toString()
            )
        } else {
            ActionExecutionResult.Failed("Could not enter text into that field.")
        }
    }

    private fun performClickOrClickableParent(node: AccessibilityNodeInfo): Boolean {
        var current: AccessibilityNodeInfo? = node
        var hops = 0
        while (current != null && hops < 4) {
            if (current.isClickable && current.isEnabled) {
                if (current.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                    return true
                }
            }
            current = current.parent
            hops++
        }
        return false
    }

    private fun findFirstScrollable(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (root.isScrollable && root.isVisibleToUser) return root
        for (i in 0 until root.childCount) {
            val child = root.getChild(i) ?: continue
            val match = findFirstScrollable(child)
            if (match != null) return match
        }
        return null
    }
}
