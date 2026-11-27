package com.elderassist.app.presentation.screens

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.elderassist.app.core.model.StitchIds
import com.elderassist.app.data.history.ActivityEvent
import com.elderassist.app.data.history.ActivityOutcome
import com.elderassist.app.feature.home.HomeUiState

@Composable
fun ActivityHistoryScreen(
    state: HomeUiState,
    onClearHistory: () -> Unit
) {
    var selectedEvent by remember { mutableStateOf<ActivityEvent?>(null) }
    var showClearConfirm by remember { mutableStateOf(false) }

    val todayEvents = state.activityHistory.filter { DateUtils.isToday(it.occurredAtEpochMs) }
    val earlierEvents = state.activityHistory.filterNot { DateUtils.isToday(it.occurredAtEpochMs) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 14.dp)
            .testTag(StitchIds.SCR_07),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Fixed UI misalignment: clean header spacing & Clear history pill alignment
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Activity History",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .weight(1f)
                    .semantics { heading() }
            )
            if (state.activityHistory.isNotEmpty()) {
                TextButton(onClick = { showClearConfirm = true }) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Clear history",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }

        if (state.activityHistory.isEmpty()) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                    Text(
                        text = "No activity yet",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Sanitized summaries of actions you run will appear here—never raw phone numbers or screen contents.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            if (todayEvents.isNotEmpty()) {
                Text(
                    text = "Today",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                todayEvents.forEach { event ->
                    ActivityHistoryRow(event = event, onClick = { selectedEvent = event })
                }
            }

            if (earlierEvents.isNotEmpty()) {
                Text(
                    text = "Earlier",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                earlierEvents.forEach { event ->
                    ActivityHistoryRow(event = event, onClick = { selectedEvent = event })
                }
            }
        }

        Spacer(modifier = Modifier.height(96.dp))
    }

    selectedEvent?.let { event ->
        val timeText = DateUtils.getRelativeTimeSpanString(
            event.occurredAtEpochMs,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS
        ).toString()

        AlertDialog(
            onDismissRequest = { selectedEvent = null },
            title = { Text(event.userSummary, style = MaterialTheme.typography.headlineSmall) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Action ID: ${event.actionCode}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Outcome: ${event.outcome.name}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "When: $timeText",
                        style = MaterialTheme.typography.bodySmall
                    )
                    event.detail?.let { detail ->
                        Text(
                            text = detail,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = { selectedEvent = null }) {
                    Text("Close")
                }
            }
        )
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear activity history?", style = MaterialTheme.typography.headlineSmall) },
            text = {
                Text(
                    text = "This removes only ElderAssist's local activity list. It does not change your contacts, phone settings, or permissions.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            dismissButton = {
                OutlinedButton(onClick = { showClearConfirm = false }) {
                    Text("Cancel")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showClearConfirm = false
                        onClearHistory()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Clear history")
                }
            }
        )
    }
}

@Composable
private fun ActivityHistoryRow(
    event: ActivityEvent,
    onClick: () -> Unit
) {
    val isSuccess = event.outcome == ActivityOutcome.SUCCESS
    val badgeBg = if (isSuccess) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.tertiaryContainer
    }
    val badgeTint = if (isSuccess) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onTertiaryContainer
    }
    val timeText = DateUtils.getRelativeTimeSpanString(
        event.occurredAtEpochMs,
        System.currentTimeMillis(),
        DateUtils.MINUTE_IN_MILLIS
    ).toString()

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(badgeBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Info,
                    contentDescription = null,
                    tint = badgeTint
                )
            }

            // Fixed UI misalignment: full title wraps cleanly across up to 2 lines instead of clipping
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.userSummary,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = timeText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Details",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
