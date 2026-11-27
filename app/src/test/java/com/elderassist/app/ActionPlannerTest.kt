package com.elderassist.app

import com.elderassist.app.core.model.ActionSource
import com.elderassist.app.core.model.AssistantAction
import com.elderassist.app.core.model.AssistantMode
import com.elderassist.app.core.model.ExecutionContext
import com.elderassist.app.core.model.RiskLevel
import com.elderassist.app.core.model.SettingDestination
import com.elderassist.app.intelligence.DefaultActionPlanner
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ActionPlannerTest {

    private val planner = DefaultActionPlanner()
    private val context = ExecutionContext(
        taskId = "t_1",
        isTaskActive = true,
        isAccessibilityEnabled = true,
        hasReadContactsPermission = true,
        hasRecordAudioPermission = true,
        mode = AssistantMode.DETERMINISTIC
    )

    @Test
    fun `createPlan marks settings low risk without confirmation`() {
        val plan = planner.createPlan(
            action = AssistantAction.OpenSetting(SettingDestination.BLUETOOTH),
            source = ActionSource.DETERMINISTIC,
            summaryForUser = "Opening Bluetooth settings.",
            context = context
        )
        assertEquals(RiskLevel.LOW, plan.riskLevel)
        assertFalse(plan.requiresConfirmation)
        assertEquals("com.android.settings", plan.expectedPackage)
    }

    @Test
    fun `createPlan marks create contact consequential write with confirmation`() {
        val plan = planner.createPlan(
            action = AssistantAction.CreateContact("Rahul", "9876543210"),
            source = ActionSource.DETERMINISTIC,
            summaryForUser = "Preparing to save Rahul.",
            context = context
        )
        assertEquals(RiskLevel.CONSEQUENTIAL_WRITE, plan.riskLevel)
        assertTrue(plan.requiresConfirmation)
    }
}
