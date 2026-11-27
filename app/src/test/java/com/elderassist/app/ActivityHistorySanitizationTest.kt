package com.elderassist.app

import com.elderassist.app.core.model.ActionId
import com.elderassist.app.core.model.AssistantAction
import com.elderassist.app.core.model.ElderAssistRoute
import com.elderassist.app.core.model.SettingDestination
import com.elderassist.app.data.history.ActivityHistoryRepository
import com.elderassist.app.data.history.ActivityOutcome
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ActivityHistorySanitizationTest {

    @Test
    fun `sanitizedMetadataFor never leaks phone numbers or contact names on writes`() {
        val (createCode, createSummary) = ActivityHistoryRepository.sanitizedMetadataFor(
            AssistantAction.CreateContact("Rahul", "+91 9876543210"),
            ActivityOutcome.SUCCESS
        )
        assertEquals(ActionId.CREATE_CONTACT.code, createCode)
        assertEquals("Created a contact", createSummary)
        assertFalse(createSummary.contains("9876543210"))
        assertFalse(createSummary.contains("Rahul"))

        val (renameCode, renameSummary) = ActivityHistoryRepository.sanitizedMetadataFor(
            AssistantAction.RenameContact(1L, "k1", "Rahul", "Rahul Sharma"),
            ActivityOutcome.SUCCESS
        )
        assertEquals(ActionId.RENAME_CONTACT.code, renameCode)
        assertEquals("Renamed a contact", renameSummary)
        assertFalse(renameSummary.contains("Rahul"))
    }

    @Test
    fun `sanitizeText redacts phone digits`() {
        val sanitized = ActivityHistoryRepository.sanitizeText("Saved number +91 9876543210")
        assertFalse(sanitized.contains("9876543210"))
        assertTrue(sanitized.contains("[redacted number]"))
    }

    @Test
    fun `settings actions map to expected ACT codes and all 9 routes are unique`() {
        val (wifiCode, wifiSummary) = ActivityHistoryRepository.sanitizedMetadataFor(
            AssistantAction.OpenSetting(SettingDestination.WIFI),
            ActivityOutcome.SUCCESS
        )
        assertEquals(ActionId.OPEN_WIFI.code, wifiCode)
        assertEquals("Opened Wi-Fi settings", wifiSummary)

        assertEquals(9, ElderAssistRoute.entries.map { it.route }.toSet().size)
        assertEquals(9, ElderAssistRoute.entries.map { it.screenId }.toSet().size)
        assertEquals(9, ElderAssistRoute.entries.map { it.routeId }.toSet().size)
    }
}
