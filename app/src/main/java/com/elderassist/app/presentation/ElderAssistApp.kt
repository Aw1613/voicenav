package com.elderassist.app.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.elderassist.app.core.model.AssistantAction
import com.elderassist.app.core.model.ElderAssistRoute
import com.elderassist.app.feature.home.HomeUiState
import com.elderassist.app.feature.home.HomeViewModel
import com.elderassist.app.presentation.components.AssistantBar
import com.elderassist.app.presentation.components.ContactWriteConfirmationDialog
import com.elderassist.app.presentation.components.ElderAssistDrawer
import com.elderassist.app.presentation.components.ElderAssistTopBar
import com.elderassist.app.presentation.screens.ActivityHistoryScreen
import com.elderassist.app.presentation.screens.AppSettingsScreen
import com.elderassist.app.presentation.screens.ContactsScreen
import com.elderassist.app.presentation.screens.HelpTutorialsScreen
import com.elderassist.app.presentation.screens.HomeScreen
import com.elderassist.app.presentation.screens.MasterAiScreen
import com.elderassist.app.presentation.screens.PermissionsPrivacyScreen
import com.elderassist.app.presentation.screens.SettingsNavigationScreen
import com.elderassist.app.presentation.screens.VoiceLanguageScreen
import com.elderassist.app.presentation.theme.ElderAssistTheme
import kotlinx.coroutines.launch

@Composable
fun ElderAssistApp(
    state: HomeUiState,
    viewModel: HomeViewModel
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRouteString = backStackEntry?.destination?.route ?: ElderAssistRoute.HOME.route
    val currentRoute = ElderAssistRoute.entries.firstOrNull { it.route == currentRouteString }
        ?: ElderAssistRoute.HOME

    fun navigateTo(route: ElderAssistRoute) {
        scope.launch { drawerState.close() }
        if (currentRoute != route) {
            navController.navigate(route.route) {
                popUpTo(ElderAssistRoute.HOME.route) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    ElderAssistTheme(
        themeMode = state.preferences.themeMode,
        textScale = state.preferences.textScale
    ) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ElderAssistDrawer(
                    currentRoute = currentRoute,
                    onSelectRoute = ::navigateTo
                )
            }
        ) {
            Scaffold(
                topBar = {
                    ElderAssistTopBar(
                        title = if (currentRoute == ElderAssistRoute.HOME) {
                            "Voice Guide"
                        } else {
                            currentRoute.label
                        },
                        isMasterAiReady = state.preferences.masterAiEnabled,
                        isListening = state.isListening,
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onHelpClick = { navigateTo(ElderAssistRoute.HELP) },
                        onProfileClick = { navigateTo(ElderAssistRoute.PRIVACY) }
                    )
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = ElderAssistRoute.HOME.route,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        composable(ElderAssistRoute.HOME.route) {
                            HomeScreen(
                                state = state,
                                onQuickSetting = viewModel::openQuickSetting,
                                onGoHome = { viewModel.triggerGlobalAction(AssistantAction.GoHome) },
                                onProposeCreateContact = viewModel::proposeCreateContact,
                                onRetryTranscript = viewModel::retryLastTranscript,
                                onStopCancel = viewModel::cancelActiveTask,
                                onSelectClarification = viewModel::selectClarificationOption,
                                onSelectContact = viewModel::selectContactCandidate,
                                onDismissContactChoice = viewModel::dismissContactSelection,
                                onOpenPermissions = { navigateTo(ElderAssistRoute.PRIVACY) }
                            )
                        }

                        composable(ElderAssistRoute.MASTER_AI.route) {
                            MasterAiScreen(
                                state = state,
                                onToggleMasterAi = viewModel::setMasterAiEnabled,
                                onStartAssistant = viewModel::onMicButtonTapped,
                                onToggleExperimentalNav = viewModel::toggleExperimentalAiNavigation,
                                onOpenAccessibilitySettings = viewModel::openAccessibilitySettings
                            )
                        }

                        composable(ElderAssistRoute.SETTINGS_NAV.route) {
                            SettingsNavigationScreen(
                                onOpenSetting = viewModel::openQuickSetting,
                                onGlobalNav = viewModel::triggerGlobalAction
                            )
                        }

                        composable(ElderAssistRoute.CONTACTS.route) {
                            ContactsScreen(
                                state = state,
                                onSearchQueryChange = viewModel::onContactSearchQueryChanged,
                                onMicSearch = viewModel::onMicButtonTapped,
                                onOpenContactsApp = viewModel::openQuickContacts,
                                onRequestContactsPermission = viewModel::requestContactsPermission,
                                onProposeCreateContact = viewModel::proposeCreateContact,
                                onProposeRenameContact = viewModel::proposeRenameContact,
                                onSelectContactChoice = viewModel::selectContactCandidate,
                                onDismissContactChoice = viewModel::dismissContactSelection
                            )
                        }

                        composable(ElderAssistRoute.VOICE_LANGUAGE.route) {
                            VoiceLanguageScreen(
                                state = state,
                                onTestMicrophone = viewModel::testMicrophone,
                                onSelectLanguage = viewModel::setSpeechLanguageTag,
                                onToggleTts = viewModel::toggleTts,
                                onSelectSpeed = viewModel::setSpeechSpeed,
                                onSelectFeedbackLevel = viewModel::setFeedbackLevel
                            )
                        }

                        composable(ElderAssistRoute.PRIVACY.route) {
                            PermissionsPrivacyScreen(
                                state = state,
                                onRequestMicPermission = viewModel::requestMicPermission,
                                onRequestContactsPermission = viewModel::requestContactsPermission,
                                onOpenAccessibilitySettings = viewModel::openAccessibilitySettings,
                                onOpenAppSettings = viewModel::openAppDetailsSettings
                            )
                        }

                        composable(ElderAssistRoute.HISTORY.route) {
                            ActivityHistoryScreen(
                                state = state,
                                onClearHistory = viewModel::clearActivityHistory
                            )
                        }

                        composable(ElderAssistRoute.HELP.route) {
                            HelpTutorialsScreen(
                                onListenExample = viewModel::speakExamplePhrase,
                                onTryCommand = { phrase ->
                                    navigateTo(ElderAssistRoute.HOME)
                                    viewModel.submitCommand(phrase)
                                }
                            )
                        }

                        composable(ElderAssistRoute.APP_SETTINGS.route) {
                            AppSettingsScreen(
                                state = state,
                                onSelectTheme = viewModel::setThemeMode,
                                onSelectTextScale = viewModel::setTextScale,
                                onToggleHaptics = viewModel::setHapticsEnabled,
                                onToggleTts = viewModel::toggleTts,
                                onOpenVoiceLanguage = { navigateTo(ElderAssistRoute.VOICE_LANGUAGE) },
                                onToggleNotifications = viewModel::setNotificationsEnabled,
                                onResetPreferences = viewModel::resetPreferences
                            )
                        }
                    }

                    // Persistent CMP-01 AssistantBar across all screens
                    AssistantBar(
                        isListening = state.isListening,
                        isTextOpen = state.isTextSheetOpen,
                        manualInput = state.manualInput,
                        primaryLabelIdle = if (currentRoute == ElderAssistRoute.HOME) {
                            "Tap to Speak"
                        } else {
                            "Ask ElderAssist"
                        },
                        onMicClick = viewModel::onMicButtonTapped,
                        onToggleText = viewModel::setTextSheetOpen,
                        onManualInputChange = viewModel::onManualInputChanged,
                        onSubmitManual = viewModel::submitManualCommand,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }

            // CMP-06 Contact write confirmation dialog
            state.pendingConfirmation?.let { pending ->
                ContactWriteConfirmationDialog(
                    pending = pending,
                    onConfirm = viewModel::confirmPendingAction,
                    onCancel = viewModel::cancelPendingAction
                )
            }
        }
    }
}
