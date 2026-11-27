package com.elderassist.app

import com.elderassist.app.core.model.AccessibleNodeSummary
import com.elderassist.app.core.model.ActionPlan
import com.elderassist.app.core.model.ActionSource
import com.elderassist.app.core.model.AssistantAction
import com.elderassist.app.core.model.AssistantMode
import com.elderassist.app.core.model.ExecutionContext
import com.elderassist.app.core.model.RiskLevel
import com.elderassist.app.core.model.ScreenSnapshot
import com.elderassist.app.core.model.SettingDestination
import com.elderassist.app.execution.ActionValidator
import com.elderassist.app.execution.ValidationResult
import org.junit.Assert.assertTrue
import org.junit.Test

class ActionValidatorTest {

    private var fakeTime = 1_000_000L
    private val validator = ActionValidator(nowMsProvider = { fakeTime })

    private fun baseContext(
        accessibilityEnabled: Boolean = true,
        confirmationGranted: Boolean = false,
        snapshot: ScreenSnapshot? = null,
        step: Int = 0
    ) = ExecutionContext(
        taskId = "task_1",
        isTaskActive = true,
        isAccessibilityEnabled = accessibilityEnabled,
        hasReadContactsPermission = true,
        hasRecordAudioPermission = true,
        mode = AssistantMode.AI_EXPERIMENTAL,
        currentSnapshot = snapshot,
        confirmationGranted = confirmationGranted,
        currentStep = step
    )

    private fun planFor(action: AssistantAction, risk: RiskLevel = RiskLevel.LOW) = ActionPlan(
        taskId = "task_1",
        createdAtEpochMs = fakeTime,
        action = action,
        requiresConfirmation = risk == RiskLevel.CONSEQUENTIAL_WRITE,
        riskLevel = risk,
        source = ActionSource.DETERMINISTIC,
        maxSteps = 5,
        summaryForUser = "Test"
    )

    @Test
    fun `create and rename contact require confirmation`() {
        val createResult = validator.validate(
            planFor(AssistantAction.CreateContact("Rahul", "9876543210"), RiskLevel.CONSEQUENTIAL_WRITE),
            baseContext(confirmationGranted = false)
        )
        assertTrue(createResult is ValidationResult.RequiresConfirmation)

        val confirmed = validator.validate(
            planFor(AssistantAction.CreateContact("Rahul", "9876543210"), RiskLevel.CONSEQUENTIAL_WRITE),
            baseContext(confirmationGranted = true)
        )
        assertTrue(confirmed is ValidationResult.Valid)
    }

    @Test
    fun `go home rejected when accessibility disabled`() {
        val result = validator.validate(
            planFor(AssistantAction.GoHome),
            baseContext(accessibilityEnabled = false)
        )
        assertTrue(result is ValidationResult.Rejected)
    }

    @Test
    fun `stale snapshot ID and disabled or missing nodes are rejected`() {
        val snapshot = ScreenSnapshot(
            snapshotId = "snap_10",
            timestampMs = fakeTime,
            packageName = "com.android.settings",
            nodes = listOf(
                AccessibleNodeSummary(nodeId = "node_1", text = "Display", clickable = true, enabled = true, visible = true),
                AccessibleNodeSummary(nodeId = "node_2", text = "Disabled", clickable = true, enabled = false, visible = true)
            )
        )

        // Stale snapshot ID
        val stale = validator.validate(
            planFor(AssistantAction.ClickNode("snap_9", "node_1"), RiskLevel.MEDIUM),
            baseContext(snapshot = snapshot)
        )
        assertTrue(stale is ValidationResult.Rejected)

        // Missing node
        val missing = validator.validate(
            planFor(AssistantAction.ClickNode("snap_10", "node_99"), RiskLevel.MEDIUM),
            baseContext(snapshot = snapshot)
        )
        assertTrue(missing is ValidationResult.Rejected)

        // Disabled node
        val disabled = validator.validate(
            planFor(AssistantAction.ClickNode("snap_10", "node_2"), RiskLevel.MEDIUM),
            baseContext(snapshot = snapshot)
        )
        assertTrue(disabled is ValidationResult.Rejected)

        // Valid node
        val valid = validator.validate(
            planFor(AssistantAction.ClickNode("snap_10", "node_1"), RiskLevel.MEDIUM),
            baseContext(snapshot = snapshot)
        )
        assertTrue(valid is ValidationResult.Valid)
    }

    @Test
    fun `excluded sensitive package and max step limit are rejected`() {
        val bankingSnapshot = ScreenSnapshot(
            snapshotId = "snap_11",
            timestampMs = fakeTime,
            packageName = "com.phonepe.app",
            nodes = listOf(
                AccessibleNodeSummary(nodeId = "node_1", text = "Pay", clickable = true, enabled = true, visible = true)
            )
        )

        val blockedPkg = validator.validate(
            planFor(AssistantAction.ClickNode("snap_11", "node_1"), RiskLevel.MEDIUM),
            baseContext(snapshot = bankingSnapshot)
        )
        assertTrue(blockedPkg is ValidationResult.Rejected)

        val settingsSnapshot = bankingSnapshot.copy(packageName = "com.android.settings")
        val maxStepsExceeded = validator.validate(
            planFor(AssistantAction.ClickNode("snap_11", "node_1"), RiskLevel.MEDIUM),
            baseContext(snapshot = settingsSnapshot, step = 5)
        )
        assertTrue(maxStepsExceeded is ValidationResult.Rejected)
    }

    @Test
    fun `prohibited risk level is always rejected`() {
        val res = validator.validate(
            planFor(AssistantAction.OpenSetting(SettingDestination.WIFI), RiskLevel.PROHIBITED),
            baseContext()
        )
        assertTrue(res is ValidationResult.Rejected)
    }
}
