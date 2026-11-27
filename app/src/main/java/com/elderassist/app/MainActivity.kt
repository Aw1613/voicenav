package com.elderassist.app

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.elderassist.app.feature.home.ElderAssistRootScreen
import com.elderassist.app.feature.home.HomeViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()

            val micLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { granted ->
                viewModel.onMicPermissionResult(granted)
            }

            val contactsLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { granted ->
                viewModel.onContactsPermissionResult(granted)
            }

            LaunchedEffect(state.requestMicPermissionPrompt) {
                if (state.requestMicPermissionPrompt) {
                    micLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            }

            LaunchedEffect(state.requestContactsPermissionPrompt) {
                if (state.requestContactsPermissionPrompt) {
                    contactsLauncher.launch(Manifest.permission.READ_CONTACTS)
                }
            }

            MaterialTheme {
                ElderAssistRootScreen(
                    state = state,
                    onMicTap = viewModel::onMicButtonTapped,
                    onStopCancel = viewModel::cancelActiveTask,
                    onManualInputChange = viewModel::onManualInputChanged,
                    onSubmitManual = viewModel::submitManualCommand,
                    onRetryTranscript = viewModel::retryLastTranscript,
                    onSelectClarification = viewModel::selectClarificationOption,
                    onSelectContact = viewModel::selectContactCandidate,
                    onDismissContactSelection = viewModel::dismissContactSelection,
                    onConfirmPending = viewModel::confirmPendingAction,
                    onCancelPending = viewModel::cancelPendingAction,
                    onOpenSettingsShortcuts = viewModel::setShowSettingsShortcuts,
                    onQuickSettingTap = viewModel::openQuickSetting,
                    onQuickContactsTap = viewModel::openQuickContacts,
                    onOpenSetupHelp = viewModel::setShowSetupHelp,
                    onRequestMicPermission = viewModel::requestMicPermission,
                    onRequestContactsPermission = viewModel::requestContactsPermission,
                    onOpenAccessibilitySettings = viewModel::openAccessibilitySettings,
                    onToggleTts = viewModel::toggleTts,
                    onToggleExperimentalAiNav = viewModel::toggleExperimentalAiNavigation
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshEnvironmentState()
    }
}
