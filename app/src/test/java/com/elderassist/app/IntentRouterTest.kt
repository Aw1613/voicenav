package com.elderassist.app

import com.elderassist.app.core.model.AssistantAction
import com.elderassist.app.core.model.AssistantMode
import com.elderassist.app.core.model.SettingDestination
import com.elderassist.app.intelligence.DefaultIntentRouter
import com.elderassist.app.intelligence.RoutingResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IntentRouterTest {

    private val router = DefaultIntentRouter()

    @Test
    fun `deterministic command resolves locally without AI`() = runTest {
        val result = router.route("Bluetooth kholo", AssistantMode.DETERMINISTIC)
        assertTrue(result is RoutingResult.Deterministic)
        assertEquals(
            AssistantAction.OpenSetting(SettingDestination.BLUETOOTH),
            (result as RoutingResult.Deterministic).action
        )
    }

    @Test
    fun `unmatched command requests AI interpretation with mode flag`() = runTest {
        val deterministicResult = router.route(
            "Help me turn down the screen glare",
            AssistantMode.DETERMINISTIC
        )
        assertTrue(deterministicResult is RoutingResult.NeedsAiInterpretation)
        assertFalse((deterministicResult as RoutingResult.NeedsAiInterpretation).allowExperimentalNavigation)

        val experimentalResult = router.route(
            "Help me turn down the screen glare",
            AssistantMode.AI_EXPERIMENTAL
        )
        assertTrue(experimentalResult is RoutingResult.NeedsAiInterpretation)
        assertTrue((experimentalResult as RoutingResult.NeedsAiInterpretation).allowExperimentalNavigation)
    }

    @Test
    fun `cancel phrase routes to Cancelled`() = runTest {
        assertEquals(
            RoutingResult.Cancelled,
            router.route("never mind", AssistantMode.AI_EXPERIMENTAL)
        )
    }
}
