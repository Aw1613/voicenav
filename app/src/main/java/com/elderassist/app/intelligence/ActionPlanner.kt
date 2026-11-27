package com.elderassist.app.intelligence

import com.elderassist.app.core.model.ActionPlan
import com.elderassist.app.core.model.ActionSource
import com.elderassist.app.core.model.AssistantAction
import com.elderassist.app.core.model.ExecutionContext
import com.elderassist.app.core.policy.ActionPolicy
import java.util.UUID

interface ActionPlanner {
    fun createPlan(
        action: AssistantAction,
        source: ActionSource,
        summaryForUser: String,
        context: ExecutionContext
    ): ActionPlan
}

class DefaultActionPlanner : ActionPlanner {

    override fun createPlan(
        action: AssistantAction,
        source: ActionSource,
        summaryForUser: String,
        context: ExecutionContext
    ): ActionPlan {
        val expectedPackage = when (action) {
            is AssistantAction.OpenSetting -> "com.android.settings"
            else -> null
        }
        return ActionPlan(
            taskId = context.taskId.ifEmpty { UUID.randomUUID().toString() },
            createdAtEpochMs = System.currentTimeMillis(),
            action = action,
            requiresConfirmation = ActionPolicy.requiresConfirmation(action),
            riskLevel = ActionPolicy.riskLevelFor(action),
            source = source,
            expectedPackage = expectedPackage,
            maxSteps = ActionPolicy.MAX_AI_NAVIGATION_STEPS,
            summaryForUser = summaryForUser
        )
    }
}
