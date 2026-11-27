package com.elderassist.app.execution

import com.elderassist.app.core.model.ActionExecutionResult
import com.elderassist.app.core.model.ActionPlan
import com.elderassist.app.core.model.ExecutionContext
import com.elderassist.app.core.model.VerificationResult

data class TaskExecutionOutcome(
    val result: ActionExecutionResult,
    val verification: VerificationResult
)

class TaskRunner(
    private val dispatcher: ActionDispatcher,
    private val verifier: ActionVerifier = ActionVerifier()
) {
    fun runPlan(plan: ActionPlan, context: ExecutionContext): TaskExecutionOutcome {
        val result = dispatcher.dispatch(plan, context)
        val verification = verifier.verify(plan.action, result)
        return TaskExecutionOutcome(result = result, verification = verification)
    }
}
