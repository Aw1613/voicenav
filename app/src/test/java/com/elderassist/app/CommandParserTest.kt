package com.elderassist.app

import com.elderassist.app.core.model.AssistantAction
import com.elderassist.app.core.model.SettingDestination
import com.elderassist.app.core.model.SupportedApp
import com.elderassist.app.intelligence.CommandParser
import com.elderassist.app.intelligence.ParseOutcome
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CommandParserTest {

    private val parser = CommandParser()

    @Test
    fun `empty and blank transcripts produce Empty`() {
        assertEquals(ParseOutcome.Empty, parser.parse(""))
        assertEquals(ParseOutcome.Empty, parser.parse("   ?!.  "))
    }

    @Test
    fun `all seven settings destinations match deterministically`() {
        val cases = listOf(
            "Open Wi-Fi" to SettingDestination.WIFI,
            "Take me to Wi-Fi settings" to SettingDestination.WIFI,
            "Open Bluetooth" to SettingDestination.BLUETOOTH,
            "Show me Bluetooth settings" to SettingDestination.BLUETOOTH,
            "Take me to hotspot" to SettingDestination.HOTSPOT,
            "Where can I turn on my hotspot?" to SettingDestination.HOTSPOT,
            "Open display settings" to SettingDestination.DISPLAY,
            "Help me change brightness" to SettingDestination.DISPLAY,
            "Open sound settings" to SettingDestination.SOUND,
            "Where can I change volume?" to SettingDestination.SOUND,
            "Open accessibility settings" to SettingDestination.ACCESSIBILITY,
            "Help me make text bigger" to SettingDestination.ACCESSIBILITY,
            "Open battery settings" to SettingDestination.BATTERY,
            "Show battery usage" to SettingDestination.BATTERY
        )

        cases.forEach { (input, expectedDestination) ->
            val outcome = parser.parse(input)
            assertTrue("Expected Matched for '$input', got $outcome", outcome is ParseOutcome.Matched)
            val action = (outcome as ParseOutcome.Matched).action
            assertEquals(AssistantAction.OpenSetting(expectedDestination), action)
        }
    }

    @Test
    fun `system navigation and cancel phrases match`() {
        assertEquals(
            AssistantAction.GoHome,
            (parser.parse("Go home") as ParseOutcome.Matched).action
        )
        assertEquals(
            AssistantAction.GoBack,
            (parser.parse("Back") as ParseOutcome.Matched).action
        )
        assertEquals(
            AssistantAction.OpenRecents,
            (parser.parse("Show recent apps") as ParseOutcome.Matched).action
        )
        assertEquals(
            AssistantAction.Cancel,
            (parser.parse("Stop doing that") as ParseOutcome.Matched).action
        )
    }

    @Test
    fun `ambiguous connection request asks clarification`() {
        val outcome = parser.parse("Turn on the connection")
        assertTrue(outcome is ParseOutcome.Ambiguous)
        val ambiguous = outcome as ParseOutcome.Ambiguous
        assertTrue(ambiguous.question.contains("Wi-Fi"))
        assertEquals(2, ambiguous.options.size)
    }

    @Test
    fun `prohibited requests are blocked safely`() {
        assertTrue(parser.parse("Delete Rahul") is ParseOutcome.ProhibitedOrUnsupported)
        assertTrue(parser.parse("Pay 500 via UPI") is ParseOutcome.ProhibitedOrUnsupported)
        assertTrue(parser.parse("Send message to Mom") is ParseOutcome.ProhibitedOrUnsupported)
        assertTrue(parser.parse("Transfer all my data to another phone") is ParseOutcome.ProhibitedOrUnsupported)
    }

    @Test
    fun `contacts open search create and rename parse cleanly`() {
        assertEquals(
            AssistantAction.OpenApp(SupportedApp.CONTACTS),
            (parser.parse("Show my phonebook") as ParseOutcome.Matched).action
        )

        val search = (parser.parse("Find Rahul") as ParseOutcome.Matched).action
        assertEquals(AssistantAction.SearchContact("Rahul"), search)

        val create = (parser.parse("Add Rahul 9876543210") as ParseOutcome.Matched).action
        assertEquals(AssistantAction.CreateContact("Rahul", "9876543210"), create)

        val rename = (parser.parse("Rename Rahul to Rahul Sharma") as ParseOutcome.Matched).action
        assertEquals(
            AssistantAction.RenameContact(
                currentDisplayName = "Rahul",
                newDisplayName = "Rahul Sharma"
            ),
            rename
        )
    }
}
