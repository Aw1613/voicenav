package com.elderassist.app.accessibility

import android.text.InputType
import android.view.accessibility.AccessibilityNodeInfo
import com.elderassist.app.core.model.AccessibleNodeSummary
import com.elderassist.app.core.model.ScreenSnapshot
import com.elderassist.app.core.policy.ActionPolicy
import java.util.concurrent.atomic.AtomicLong

class AccessibilityTreeReader {

    companion object {
        private const val MAX_DEPTH = 12
        private const val MAX_NODES = 60
        private val snapshotCounter = AtomicLong(0L)
    }

    @Volatile
    private var activeSnapshotId: String? = null

    @Volatile
    private var activePackageName: String? = null

    @Volatile
    private var activeLocators: Map<String, NodeLocator> = emptyMap()

    fun captureSnapshot(
        root: AccessibilityNodeInfo?,
        windowTitle: String? = null
    ): ScreenSnapshot? {
        if (root == null) {
            clearSnapshot()
            return null
        }
        val packageName = root.packageName?.toString().orEmpty()
        if (packageName.isEmpty() || ActionPolicy.isPackageExcluded(packageName)) {
            clearSnapshot()
            return null
        }

        val snapshotId = "snapshot_${snapshotCounter.incrementAndGet()}_${System.currentTimeMillis()}"
        val nodes = mutableListOf<AccessibleNodeSummary>()
        val locators = mutableMapOf<String, NodeLocator>()

        traverse(
            node = root,
            packageName = packageName,
            snapshotId = snapshotId,
            path = emptyList(),
            depth = 0,
            outNodes = nodes,
            outLocators = locators
        )

        activeSnapshotId = snapshotId
        activePackageName = packageName
        activeLocators = locators

        return ScreenSnapshot(
            snapshotId = snapshotId,
            timestampMs = System.currentTimeMillis(),
            packageName = packageName,
            windowTitle = windowTitle,
            nodes = nodes
        )
    }

    fun resolveLocator(snapshotId: String, nodeId: String): NodeLocator? {
        if (snapshotId != activeSnapshotId) return null
        return activeLocators[nodeId]
    }

    fun activePackage(): String? = activePackageName

    fun clearSnapshot() {
        activeSnapshotId = null
        activePackageName = null
        activeLocators = emptyMap()
    }

    private fun traverse(
        node: AccessibilityNodeInfo,
        packageName: String,
        snapshotId: String,
        path: List<Int>,
        depth: Int,
        outNodes: MutableList<AccessibleNodeSummary>,
        outLocators: MutableMap<String, NodeLocator>
    ) {
        if (depth > MAX_DEPTH || outNodes.size >= MAX_NODES) return

        val sensitive = isSensitiveNode(node)
        if (sensitive) {
            // Skip password/OTP/sensitive subtrees entirely for privacy.
            return
        }

        val visible = node.isVisibleToUser
        val rawText = node.text?.toString()?.trim()?.takeIf { it.isNotEmpty() }
        val rawDesc = node.contentDescription?.toString()?.trim()?.takeIf { it.isNotEmpty() }
        val isInteractive = node.isClickable || node.isScrollable || node.isEditable
        val hasMeaningfulInfo = rawText != null || rawDesc != null || isInteractive

        if (visible && hasMeaningfulInfo) {
            val nodeId = "node_${outNodes.size + 1}"
            val className = node.className?.toString()
            outNodes += AccessibleNodeSummary(
                nodeId = nodeId,
                text = rawText?.take(120),
                contentDescription = rawDesc?.take(120),
                className = className,
                clickable = node.isClickable,
                enabled = node.isEnabled,
                visible = visible,
                scrollable = node.isScrollable,
                editable = node.isEditable,
                isSensitive = false
            )
            outLocators[nodeId] = NodeLocator(
                snapshotId = snapshotId,
                childIndexPath = path,
                expectedClassName = className,
                expectedText = rawText,
                expectedContentDescription = rawDesc,
                expectedPackageName = packageName
            )
        }

        val count = node.childCount
        for (i in 0 until count) {
            if (outNodes.size >= MAX_NODES) break
            val child = node.getChild(i) ?: continue
            traverse(
                node = child,
                packageName = packageName,
                snapshotId = snapshotId,
                path = path + i,
                depth = depth + 1,
                outNodes = outNodes,
                outLocators = outLocators
            )
        }
    }

    private fun isSensitiveNode(node: AccessibilityNodeInfo): Boolean {
        if (node.isPassword) return true
        val inputType = node.inputType
        val variation = inputType and InputType.TYPE_MASK_VARIATION
        val isPasswordVariation = variation == InputType.TYPE_TEXT_VARIATION_PASSWORD ||
            variation == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD ||
            variation == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD ||
            variation == InputType.TYPE_NUMBER_VARIATION_PASSWORD
        if (isPasswordVariation) return true

        val hintOrId = buildString {
            append(node.viewIdResourceName.orEmpty())
            append(' ')
            append(node.hintText?.toString().orEmpty())
        }.lowercase()
        return hintOrId.contains("password") ||
            hintOrId.contains("otp") ||
            hintOrId.contains("pin_code") ||
            hintOrId.contains("cvv")
    }
}
