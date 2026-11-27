package com.elderassist.app.presentation.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.elderassist.app.core.model.ActionId
import com.elderassist.app.core.model.ContactCandidate
import com.elderassist.app.core.model.StitchIds
import com.elderassist.app.feature.home.HomeUiState

@Composable
fun ContactsScreen(
    state: HomeUiState,
    onSearchQueryChange: (String) -> Unit,
    onMicSearch: () -> Unit,
    onOpenContactsApp: () -> Unit,
    onRequestContactsPermission: () -> Unit,
    onProposeCreateContact: (String, String) -> Unit,
    onProposeRenameContact: (String, String) -> Unit,
    onSelectContactChoice: (ContactCandidate) -> Unit,
    onDismissContactChoice: () -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameInitialOldName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 14.dp)
            .testTag(StitchIds.SCR_04),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Contacts",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.semantics { heading() }
            )
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.tertiaryContainer
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Safe & Simple",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }

        Text(
            text = "Search by name or tap an option below to get started.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Search Bar
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 10.dp)
                )
                OutlinedTextField(
                    value = state.contactSearchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Search by name", style = MaterialTheme.typography.bodyMedium) },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = onMicSearch,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Search by voice",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        if (!state.hasContactsPermission && state.contactSearchQuery.isNotBlank()) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Contacts permission is needed to search names inside ElderAssist.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Button(
                        onClick = onRequestContactsPermission,
                        shape = CircleShape,
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text("Allow Contacts Access", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        } else if (state.contactSearchResults.isNotEmpty()) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Matching Contacts",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    state.contactSearchResults.forEach { contact ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                                .clickable {
                                    renameInitialOldName = contact.displayName
                                    showRenameDialog = true
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = contact.displayName,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                contact.phoneHint?.let {
                                    Text(
                                        text = "Phone $it",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Text(
                                text = "Rename",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // Open Contacts Button (ACT-08)
        Button(
            onClick = onOpenContactsApp,
            shape = CircleShape,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag(ActionId.OPEN_CONTACTS.code)
        ) {
            Icon(imageVector = Icons.Default.Contacts, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Open Contacts", style = MaterialTheme.typography.labelLarge)
        }

        // Live disambiguation card if voice triggered multiple contact matches
        state.pendingContactChoice?.let { choice ->
            HomeContactChoiceCard(
                choice = choice,
                onSelect = onSelectContactChoice,
                onDismiss = onDismissContactChoice
            )
        }

        Text(
            text = "Quick Actions",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Fixed UI misalignment: Full non-truncated descriptions on all Quick Action rows
        ContactQuickActionCard(
            icon = Icons.Default.PersonSearch,
            badgeBg = MaterialTheme.colorScheme.tertiaryContainer,
            badgeTint = MaterialTheme.colorScheme.onTertiaryContainer,
            title = "Find a contact",
            description = "Say or type someone's name to check their details",
            onClick = { onSearchQueryChange(state.contactSearchQuery.ifBlank { "a" }) },
            testTag = ActionId.SEARCH_CONTACT.code
        )

        ContactQuickActionCard(
            icon = Icons.Default.PersonAdd,
            badgeBg = MaterialTheme.colorScheme.secondaryContainer,
            badgeTint = MaterialTheme.colorScheme.onSecondaryContainer,
            title = "Add a contact",
            description = "Save a new family member or helper with confirmation",
            onClick = { showCreateDialog = true },
            testTag = ActionId.CREATE_CONTACT.code
        )

        ContactQuickActionCard(
            icon = Icons.Default.Edit,
            badgeBg = MaterialTheme.colorScheme.primaryContainer,
            badgeTint = MaterialTheme.colorScheme.onPrimaryContainer,
            title = "Rename a contact",
            description = "Update a saved contact name after reviewing the preview",
            onClick = {
                renameInitialOldName = ""
                showRenameDialog = true
            },
            testTag = ActionId.RENAME_CONTACT.code
        )

        // Helpful example preview matching Stitch layout
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.RecordVoiceOver,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        text = "Voice Assistant Tip",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
                Text(
                    text = "If multiple people share a name like \"Rahul\", ElderAssist always asks you to choose before making any change.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(96.dp))
    }

    if (showCreateDialog) {
        AddContactDialog(
            onDismiss = { showCreateDialog = false },
            onSubmit = { name, phone ->
                showCreateDialog = false
                onProposeCreateContact(name, phone)
            }
        )
    }

    if (showRenameDialog) {
        RenameContactDialog(
            initialOldName = renameInitialOldName,
            onDismiss = { showRenameDialog = false },
            onSubmit = { oldName, newName ->
                showRenameDialog = false
                onProposeRenameContact(oldName, newName)
            }
        )
    }
}

@Composable
private fun ContactQuickActionCard(
    icon: ImageVector,
    badgeBg: Color,
    badgeTint: Color,
    title: String,
    description: String,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(badgeBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = badgeTint,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AddContactDialog(
    onDismiss: () -> Unit,
    onSubmit: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Add a Contact",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone number") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = CircleShape,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = { onSubmit(name, phone) },
                        enabled = name.isNotBlank() && phone.filter { it.isDigit() }.length >= 3,
                        shape = CircleShape,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                    ) {
                        Text("Review")
                    }
                }
            }
        }
    }
}

@Composable
fun RenameContactDialog(
    initialOldName: String,
    onDismiss: () -> Unit,
    onSubmit: (String, String) -> Unit
) {
    var oldName by remember { mutableStateOf(initialOldName) }
    var newName by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Rename a Contact",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                OutlinedTextField(
                    value = oldName,
                    onValueChange = { oldName = it },
                    label = { Text("Current contact name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("New name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = CircleShape,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = { onSubmit(oldName, newName) },
                        enabled = oldName.isNotBlank() && newName.isNotBlank(),
                        shape = CircleShape,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                    ) {
                        Text("Review")
                    }
                }
            }
        }
    }
}
