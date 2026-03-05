@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.sakethh.limae.ui.screens.settings

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sakethh.limae.domain.SuggestionEngine
import com.sakethh.limae.platform.Platform
import com.sakethh.limae.platform.platform
import com.sakethh.limae.ui.Icons
import com.sakethh.limae.ui.LimaeAction
import com.sakethh.limae.ui.common.ConfirmationDialog
import com.sakethh.limae.ui.common.ItemState
import com.sakethh.limae.ui.common.showHandOnHover
import com.sakethh.limae.utils.Constants
import com.sakethh.limae.utils.LimaePreferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import limae.app.generated.resources.Res
import limae.app.generated.resources.discord
import limae.app.generated.resources.github
import limae.app.generated.resources.secretary_bird_webp
import limae.app.generated.resources.twitter
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(performAction: (LimaeAction) -> Unit) {
    val topAppBarScrollBehaviour = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val platformVersion = platform.version
    val onAndroid =
        rememberSaveable {
            platform.type == Platform.Type.AndroidMobile || platform.type == Platform.Type.AndroidTablet
        }
    val isSystemInDarkTheme = isSystemInDarkTheme()
    var showNewCustomStringForDictBtmSheet by rememberSaveable {
        mutableStateOf(false)
    }
    val newCustomStringInDictBtmSheet = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val newCustomStringInDictBtmSheetFocus =
        retain {
            FocusRequester()
        }
    val settingsScreenVM: SettingsScreenVM = koinViewModel()
    val dictionaryStrings by settingsScreenVM.dictionaryStrings.collectAsStateWithLifecycle()
    val isDictEmpty = dictionaryStrings.isEmpty()
    var showDeleteAllDictStringsDialogBox by rememberSaveable {
        mutableStateOf(false)
    }
    var showDeleteAllDraftsDialogBox by rememberSaveable {
        mutableStateOf(false)
    }
    var showBlockAppsList by rememberSaveable {
        mutableStateOf(false)
    }

    val blockListIcnBtnRotation by animateFloatAsState(targetValue = if (showBlockAppsList) 90f else -90f)

    val installedAppsState by settingsScreenVM.installedApps.collectAsStateWithLifecycle(
        initialValue =
            ItemState(
                isError = false,
                errorMessage = null,
                isLoading = true,
                data = emptyList(),
            ),
    )

    val appBlockList by settingsScreenVM.appBlockList.collectAsStateWithLifecycle()

    val areSearchResultsEmpty =
        settingsScreenVM.appSearchQuery.isNotBlank() && installedAppsState.data.isEmpty() &&
            !installedAppsState.isLoading

    var showAccessibilityIconSizePreview by rememberSaveable {
        mutableStateOf(false)
    }

    var showImportExportProgressDialog by rememberSaveable {
        mutableStateOf(false)
    }
    var importExportLabel by rememberSaveable {
        mutableStateOf("Importing...")
    }
    LaunchedEffect(Unit) {
        if (!onAndroid) return@LaunchedEffect

        snapshotFlow {
            LimaePreferences.accessibilityIconSize
        }.drop(1).collectLatest {
            showAccessibilityIconSizePreview = true
            delay(1000L)
            showAccessibilityIconSizePreview = false
        }
    }
    val accessibilityIconSize by animateDpAsState(LimaePreferences.accessibilityIconSize.dp)
    val isExportPathPicked =
        !onAndroid || (onAndroid && LimaePreferences.exportDirPath.isNotBlank())

    val exportComponent: @Composable () -> Unit = {
        SettingComponent(
            SettingComponentParam(
                title = "Export",
                doesDescriptionExists = true,
                description = "Export drafts, dictionary, and blocklist to ${if (!onAndroid) "'Documents/Limae/Exports' as" else "a"} JSON file",
                isSwitchNeeded = false,
                isSwitchEnabled = LimaePreferences.autoSaveNotes,
                onSwitchStateChange = {
                    settingsScreenVM.performAction(
                        SettingsScreenAction.ExportData(onStart = {
                            importExportLabel = "Exporting..."
                            showImportExportProgressDialog = true
                        }, onCompletion = {
                            showImportExportProgressDialog = false
                        }),
                    )
                },
                showIcon = true,
                icon = Icons.DataObject,
                showFilledIcon = true,
            ),
        )
    }

    val exportedPathComponentSpace = if (isExportPathPicked) 15.dp else 0.dp

    val localUriHandler = LocalUriHandler.current
    Scaffold(topBar = {
        LargeTopAppBar(title = {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.titleMedium,
                fontSize = 24.sp,
            )
        }, navigationIcon = {
            IconButton(modifier = Modifier.showHandOnHover(), onClick = {
                performAction(LimaeAction.NavigateBack)
            }) {
                Icon(
                    imageVector = Icons.ArrowBack,
                    contentDescription = "Icon button to navigate back to main screen",
                )
            }
        }, scrollBehavior = topAppBarScrollBehaviour)
    }, modifier = Modifier.fillMaxSize()) { paddingValues ->
        LazyColumn(
            contentPadding = paddingValues,
            modifier =
                Modifier
                    .fillMaxSize()
                    .nestedScroll(topAppBarScrollBehaviour.nestedScrollConnection),
        ) {
            item {
                Column(
                    modifier =
                        Modifier
                            .padding(
                                start = 15.dp,
                                end = 15.dp,
                                top = 15.dp,
                                bottom = 7.5.dp,
                            ).clip(
                                RoundedCornerShape(15.dp),
                            ).fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(top = 7.5.dp),
                ) {
                    AppVersionLabel()
                    Row(
                        modifier = Modifier.padding(start = 10.dp, top = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        FilledIconButton(
                            modifier =
                                Modifier
                                    .pointerHoverIcon(icon = PointerIcon.Hand),
                            onClick = {
                                localUriHandler.openUri("https://www.github.com/LimaeApp")
                            },
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.github),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                        FilledIconButton(
                            modifier =
                                Modifier
                                    .pointerHoverIcon(icon = PointerIcon.Hand),
                            onClick = {
                                localUriHandler.openUri("https://discord.gg/ZDBXNtv8MD")
                            },
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.discord),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                        FilledIconButton(
                            modifier =
                                Modifier
                                    .pointerHoverIcon(icon = PointerIcon.Hand),
                            onClick = {
                                localUriHandler.openUri("https://www.twitter.com/LimaeApp")
                            },
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.twitter),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    }

                    Column(
                        modifier =
                            Modifier
                                .padding(7.5.dp)
                                .clip(RoundedCornerShape(25.dp))
                                .background(MaterialTheme.colorScheme.onSecondary)
                                .fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(15.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Image(
                                painter = painterResource(Res.drawable.secretary_bird_webp),
                                contentDescription = null,
                                modifier =
                                    Modifier.size(65.dp).clip(CircleShape).border(
                                        width = 1.5.dp,
                                        color = MaterialTheme.colorScheme.secondary,
                                        shape = CircleShape,
                                    ),
                            )
                            Spacer(Modifier.width(15.dp))
                            Text(
                                text = "Limae's mascot, the Secretary Bird, is an artwork by Maxime Budar.",
                                style = MaterialTheme.typography.titleSmall,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.secondary,
                            )
                        }
                        Spacer(Modifier.height(2.5.dp))
                        Button(
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiary,
                                    contentColor = MaterialTheme.colorScheme.onTertiary,
                                ),
                            modifier =
                                Modifier
                                    .padding(start = 15.dp, end = 15.dp, bottom = 15.dp)
                                    .fillMaxWidth()
                                    .showHandOnHover(),
                            onClick = {
                                localUriHandler.openUri("https://www.artstation.com/maximebudar")
                            },
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.OpenInNew,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(Modifier.width(2.5.dp))
                                Text(
                                    text = "Maxime on ArtStation",
                                    style = MaterialTheme.typography.titleSmall,
                                )
                            }
                        }
                    }
                    Button(
                        onClick = {
                            localUriHandler.openUri("https://ko-fi.com/sakethpathike")
                        },
                        modifier =
                            Modifier
                                .pointerHoverIcon(icon = PointerIcon.Hand)
                                .padding(start = 15.dp, end = 15.dp),
                    ) {
                        Icon(imageVector = Icons.FilledCoffee, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.5.dp))
                        Text(
                            text = "Buy me a Coffee",
                            style = MaterialTheme.typography.titleMedium,
                            fontSize = 16.5.sp,
                        )
                    }
                    /*Button(
                        onClick = {
                            uriHandler.openUri("https://play.google.com/store/apps/details?id=com.sakethh.linkora")
                        },
                        modifier =
                            Modifier
                                .pointerHoverIcon(icon = PointerIcon.Hand)
                                .padding(start = 15.dp, bottom = 15.dp)
                                .pressScaleEffect(),
                    ) {
                        Icon(imageVector = Icons.Default.RateReview, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.5.dp))
                        Text(
                            text = Localization.Key.RateOnPlayLabel.rememberLocalizedString(),
                            style = MaterialTheme.typography.titleMedium,
                            fontSize = 16.5.sp,
                        )
                    }*/
                    Spacer(Modifier.height(15.dp))
                }
            }
            item {
                Text(
                    text = "Theme",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(15.dp),
                    fontSize = 16.sp,
                )
            }

            if (onAndroid && !LimaePreferences.useDarkTheme) {
                item {
                    SettingComponent(
                        SettingComponentParam(
                            title = "Follow System Theme",
                            doesDescriptionExists = false,
                            isSwitchNeeded = true,
                            description = null,
                            isSwitchEnabled = LimaePreferences.useSystemTheme,
                            onSwitchStateChange = {
                                LimaePreferences.useSystemTheme = !LimaePreferences.useSystemTheme

                                settingsScreenVM.updatePreference(
                                    preferenceKey =
                                        Platform.Preferences.Key.BooleanPreferencesKey(
                                            LimaePreferences.Key.USE_SYSTEM_THEME.name,
                                        ),
                                    newValue = LimaePreferences.useSystemTheme,
                                )
                            },
                            showIcon = false,
                        ),
                    )
                    Spacer(modifier = Modifier.height(15.dp))
                }
            }
            if (!LimaePreferences.useSystemTheme || platform.type == Platform.Type.Desktop) {
                item(key = "Use Dark Mode") {
                    SettingComponent(
                        SettingComponentParam(
                            title = "Use Dark Mode",
                            doesDescriptionExists = false,
                            description = null,
                            isSwitchNeeded = true,
                            isSwitchEnabled = LimaePreferences.useDarkTheme,
                            onSwitchStateChange = {
                                LimaePreferences.useDarkTheme = !LimaePreferences.useDarkTheme
                                settingsScreenVM.updatePreference(
                                    preferenceKey =
                                        Platform.Preferences.Key.BooleanPreferencesKey(
                                            LimaePreferences.Key.USE_DARK_THEME.name,
                                        ),
                                    newValue = LimaePreferences.useDarkTheme,
                                )
                            },
                            showIcon = false,
                        ),
                    )
                    Spacer(modifier = Modifier.height(15.dp))
                }
            }
            if (onAndroid && (LimaePreferences.useDarkTheme || (isSystemInDarkTheme && LimaePreferences.useSystemTheme))) {
                item {
                    SettingComponent(
                        SettingComponentParam(
                            title = "Use Amoled Theme",
                            doesDescriptionExists = false,
                            description = "",
                            isSwitchNeeded = true,
                            isSwitchEnabled = LimaePreferences.useAmoledTheme,
                            onSwitchStateChange = {
                                LimaePreferences.useAmoledTheme = !LimaePreferences.useAmoledTheme
                                settingsScreenVM.updatePreference(
                                    preferenceKey =
                                        Platform.Preferences.Key.BooleanPreferencesKey(
                                            LimaePreferences.Key.USE_AMOLED_THEME.name,
                                        ),
                                    newValue = LimaePreferences.useAmoledTheme,
                                )
                            },
                            showIcon = false,
                        ),
                    )
                    Spacer(modifier = Modifier.height(15.dp))
                }
            }
            if (onAndroid && platformVersion != null && platformVersion >= 31) {
                item {
                    SettingComponent(
                        SettingComponentParam(
                            title = "Use Dynamic Theming",
                            doesDescriptionExists = false,
                            description = "",
                            isSwitchNeeded = true,
                            isSwitchEnabled = LimaePreferences.useDynamicTheming,
                            onSwitchStateChange = {
                                LimaePreferences.useDynamicTheming =
                                    !LimaePreferences.useDynamicTheming
                                settingsScreenVM.updatePreference(
                                    preferenceKey =
                                        Platform.Preferences.Key.BooleanPreferencesKey(
                                            LimaePreferences.Key.USE_DYNAMIC_THEME.name,
                                        ),
                                    newValue = LimaePreferences.useDynamicTheming,
                                )
                            },
                            showIcon = false,
                        ),
                    )
                    Spacer(modifier = Modifier.height(15.dp))
                }
            }
            item {
                Text(
                    text = "General",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 15.dp, end = 15.dp, bottom = 15.dp),
                    fontSize = 16.sp,
                )
            }
            item {
                SettingComponent(
                    SettingComponentParam(
                        title = "Auto-save while typing",
                        doesDescriptionExists = true,
                        description = "Limae auto-saves your drafts while you type, syncing at 0.5-second intervals during keystroke pauses.",
                        isSwitchNeeded = true,
                        isSwitchEnabled = LimaePreferences.autoSaveNotes,
                        onSwitchStateChange = {
                            LimaePreferences.autoSaveNotes = !LimaePreferences.autoSaveNotes
                            settingsScreenVM.updatePreference(
                                preferenceKey =
                                    Platform.Preferences.Key.BooleanPreferencesKey(
                                        LimaePreferences.Key.AUTO_SAVE_NOTE.name,
                                    ),
                                newValue = LimaePreferences.autoSaveNotes,
                            )
                        },
                        showIcon = false,
                    ),
                )
                Spacer(modifier = Modifier.height(18.dp))
            }
            item {
                Text(
                    text = "Engines",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(start = 15.dp),
                )
                Spacer(Modifier.height(2.5.dp))
                Text(
                    text = "The selected engines will be used for suggestions. At least one must be opted in.",
                    style = MaterialTheme.typography.titleSmall,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 15.dp),
                )
                Spacer(Modifier.height(5.dp))
                SuggestionEngine.entries.forEach { suggestionEngine ->
                    if (Platform.onAndroid && suggestionEngine == SuggestionEngine.LanguageTool) return@forEach

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clickable(indication = null, interactionSource = null, onClick = {
                                    LimaePreferences.toggleOptedEngine(suggestionEngine)
                                })
                                .showHandOnHover(),
                    ) {
                        Checkbox(
                            checked = LimaePreferences.optedEngines.contains(suggestionEngine),
                            onCheckedChange = {
                                LimaePreferences.toggleOptedEngine(suggestionEngine)
                            },
                        )
                        Text(
                            text = suggestionEngine.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontSize = 16.sp,
                        )
                    }
                }
            }
            if (onAndroid) {
                item {
                    Column(modifier = Modifier.padding(start = 15.dp, end = 15.dp)) {
                        Text(
                            text = "Accessibility Overlay",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 15.dp),
                            fontSize = 16.sp,
                        )
                        Row {
                            Text(
                                text = "Icon size — ",
                                style = MaterialTheme.typography.titleMedium,
                                fontSize = 16.sp,
                                modifier = Modifier.alignByBaseline(),
                            )
                            AnimatedContent(
                                LimaePreferences.accessibilityIconSize.toString(),
                            ) { iconSize ->
                                Text(
                                    text = iconSize,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontSize = 16.sp,
                                    modifier = Modifier.alignByBaseline(),
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(15.dp))

                        Slider(
                            value = LimaePreferences.accessibilityIconSize.toFloat(),
                            onValueChange = {
                                LimaePreferences.accessibilityIconSize = it.toInt()
                            },
                            valueRange = 45f..225f,
                            steps = 45,
                        )
                    }
                    Spacer(modifier = Modifier.height(5.dp))
                }

                item {
                    Row(
                        modifier =
                            Modifier
                                .padding(start = 15.dp, end = 15.dp)
                                .clickable(indication = null, interactionSource = null) {
                                    showBlockAppsList = !showBlockAppsList
                                }.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "Blocklist",
                            style = MaterialTheme.typography.titleMedium,
                            fontSize = 16.sp,
                        )
                        IconButton(modifier = Modifier.showHandOnHover(), onClick = {
                            showBlockAppsList = !showBlockAppsList
                        }) {
                            Icon(
                                modifier = Modifier.rotate(blockListIcnBtnRotation),
                                imageVector = Icons.ArrowBack,
                                contentDescription = "Expand/shrink icon button to take add/remove apps considered for limae suggestions",
                            )
                        }
                    }
                    Text(
                        text = "Limae will not show up as a suggestion overlay nor process any text input in apps that are on the blocklist.",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier =
                            Modifier.padding(
                                start = 15.dp,
                                end = 15.dp,
                                bottom = animateDpAsState(if (showBlockAppsList) 15.dp else 0.dp).value,
                            ),
                        fontSize = 15.sp,
                    )
                    AnimatedVisibility(
                        visible = showBlockAppsList,
                        enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                        exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut(),
                    ) {
                        Column(
                            modifier =
                                Modifier
                                    .padding(start = 15.dp, end = 15.dp)
                                    .clip(RoundedCornerShape(25.dp))
                                    .border(
                                        width = 1.5.dp,
                                        color = MaterialTheme.colorScheme.outline.copy(0.15f),
                                        shape = RoundedCornerShape(25.dp),
                                    ).background(
                                        MaterialTheme.colorScheme.primaryContainer.copy(0.15f),
                                    ).height(300.dp)
                                    .fillMaxSize(),
                        ) {
                            TextField(
                                leadingIcon = {
                                    Icon(imageVector = Icons.Search, contentDescription = null)
                                },
                                textStyle = MaterialTheme.typography.titleSmall,
                                label = {
                                    Text(
                                        text = "Search for the apps",
                                        style = MaterialTheme.typography.titleSmall,
                                    )
                                },
                                value = settingsScreenVM.appSearchQuery,
                                onValueChange = {
                                    settingsScreenVM.performAction(
                                        SettingsScreenAction.UpdateAppSearchQuery(
                                            it,
                                        ),
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                            )
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                            ) {
                                item {
                                    AnimatedVisibility(installedAppsState.isLoading) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier.height(300.dp).fillMaxWidth(),
                                        ) {
                                            ContainedLoadingIndicator()
                                        }
                                    }
                                }
                                if (installedAppsState.isLoading) return@LazyColumn
                                if (!areSearchResultsEmpty) {
                                    item {
                                        Spacer(modifier = Modifier.height(7.5.dp))
                                    }
                                }
                                items(installedAppsState.data) { app ->
                                    Row(
                                        modifier =
                                            Modifier
                                                .padding(
                                                    start = 15.dp,
                                                    end = 15.dp,
                                                    top = 7.5.dp,
                                                    bottom = 7.5.dp,
                                                ).fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Column(modifier = Modifier.fillMaxWidth(0.75f)) {
                                            if (app.name.isNotBlank()) {
                                                Text(
                                                    text = app.name,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontSize = 16.sp,
                                                )
                                            }
                                            Text(
                                                text = app.packageName,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontSize = 14.sp,
                                            )
                                        }
                                        IconButton(onClick = {
                                            settingsScreenVM.performAction(
                                                SettingsScreenAction.OnBlockAnApp(
                                                    packageName = app.packageName,
                                                ),
                                            )
                                        }) {
                                            AnimatedContent(
                                                if (appBlockList.contains(
                                                        app.packageName,
                                                    )
                                                ) {
                                                    Icons.Remove
                                                } else {
                                                    Icons.Add
                                                },
                                            ) { icon ->
                                                Icon(
                                                    imageVector = icon,
                                                    contentDescription = "Add/remove app to/from block list",
                                                )
                                            }
                                        }
                                    }
                                }
                                item {
                                    AnimatedVisibility(visible = areSearchResultsEmpty) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier =
                                                Modifier
                                                    .padding(15.dp),
                                        ) {
                                            Text(
                                                text = "No results found for your query!",
                                                fontSize = 16.sp,
                                                style = MaterialTheme.typography.titleMedium,
                                            )
                                        }
                                    }
                                }
                                if (!areSearchResultsEmpty) {
                                    item {
                                        Spacer(modifier = Modifier.height(7.5.dp))
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(15.dp))
                }
            } else {
                item {
                    Spacer(modifier = Modifier.height(7.5.dp))
                }
            }
            item {
                Text(
                    text = "Data",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 15.dp, end = 15.dp, bottom = 15.dp),
                    fontSize = 16.sp,
                )
            }
            item {
                AnimatedVisibility(isExportPathPicked) {
                    SettingComponent(
                        SettingComponentParam(
                            title = "Auto-exports",
                            doesDescriptionExists = true,
                            description = "Limae auto-exports ${if (onAndroid) "" else "to 'Documents/Limae/Backups' "}on every create, update, or delete operation. This applies to drafts, the dictionary, and blocklist changes. A maximum of 25 backups are kept at any time.",
                            isSwitchNeeded = true,
                            isSwitchEnabled = LimaePreferences.useAutoExports,
                            onSwitchStateChange = {
                                LimaePreferences.useAutoExports = !LimaePreferences.useAutoExports
                                settingsScreenVM.updatePreference(
                                    preferenceKey =
                                        Platform.Preferences.Key.BooleanPreferencesKey(
                                            LimaePreferences.Key.USE_SNAPSHOTS.name,
                                        ),
                                    newValue = LimaePreferences.useAutoExports,
                                )
                            },
                            showIcon = false,
                            icon = Icons.DataObject,
                            showFilledIcon = true,
                        ),
                    )
                }
                if (isExportPathPicked || !onAndroid) {
                    Spacer(modifier = Modifier.height(22.dp))
                }
            }
            item {
                SettingComponent(
                    SettingComponentParam(
                        title = "Import",
                        doesDescriptionExists = true,
                        description = "Import from a JSON file based on the Limae Schema",
                        isSwitchNeeded = false,
                        isSwitchEnabled = LimaePreferences.autoSaveNotes,
                        onSwitchStateChange = {
                            settingsScreenVM.performAction(
                                SettingsScreenAction.ImportData(onStart = {
                                    importExportLabel = "Importing..."
                                    showImportExportProgressDialog = true
                                }, onCompletion = {
                                    showImportExportProgressDialog = false
                                }),
                            )
                        },
                        showIcon = true,
                        icon = Icons.DataObject,
                        showFilledIcon = true,
                    ),
                )
                Spacer(modifier = Modifier.height(if (isExportPathPicked) 22.dp else 18.dp))
            }
            item {
                AnimatedVisibility(!onAndroid && isExportPathPicked) {
                    exportComponent()
                }
                if (onAndroid) {
                    Column(
                        modifier =
                            Modifier
                                .padding(
                                    start = 15.dp,
                                    end = 15.dp,
                                ).then(
                                    if (isExportPathPicked) {
                                        Modifier
                                            .clip(RoundedCornerShape(25.dp))
                                            .border(
                                                width = 1.5.dp,
                                                color = MaterialTheme.colorScheme.outline.copy(0.15f),
                                                shape = RoundedCornerShape(25.dp),
                                            )
                                    } else {
                                        Modifier
                                    },
                                ),
                    ) {
                        AnimatedVisibility(isExportPathPicked) {
                            Column {
                                Spacer(modifier = Modifier.height(15.dp))
                                exportComponent()

                                Column(
                                    modifier =
                                        Modifier.padding(
                                            start = 15.dp,
                                            end = 15.dp,
                                            top = 15.dp,
                                        ),
                                ) {
                                    Text(
                                        text = "Current export path",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.secondary,
                                    )
                                    Spacer(Modifier.height(2.5.dp))
                                    Text(
                                        text = LimaePreferences.exportDirPath,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.secondary,
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                        FilledTonalButton(
                            onClick = {
                                settingsScreenVM.performAction(SettingsScreenAction.PickADirectory)
                            },
                            modifier =
                                Modifier
                                    .showHandOnHover()
                                    .padding(
                                        start = exportedPathComponentSpace,
                                        end = exportedPathComponentSpace,
                                        bottom = exportedPathComponentSpace,
                                    ).fillMaxWidth(),
                        ) {
                            Text(
                                text = "Choose an export location",
                                style = MaterialTheme.typography.titleSmall,
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
            }
            item {
                SettingComponent(
                    SettingComponentParam(
                        title = "Delete all notes",
                        doesDescriptionExists = false,
                        description = "",
                        isSwitchNeeded = false,
                        isSwitchEnabled = LimaePreferences.autoSaveNotes,
                        onSwitchStateChange = {
                            showDeleteAllDraftsDialogBox = true
                        },
                        showIcon = true,
                        icon = Icons.Delete,
                        showFilledIcon = true,
                    ),
                )
                Spacer(modifier = Modifier.height(20.dp))
            }
            stickyHeader {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface),
                ) {
                    Text(
                        text = "Dictionary",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier =
                            Modifier.padding(
                                start = 15.dp,
                                end = 15.dp,
                                bottom = 5.dp,
                            ),
                        fontSize = 16.sp,
                    )
                    Text(
                        text = "The dictionary contains your custom strings, which Limae will filter out during suggestions.",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(start = 15.dp, end = 15.dp, bottom = 7.5.dp),
                        fontSize = 15.sp,
                    )
                    FilledTonalButton(
                        onClick = {
                            showNewCustomStringForDictBtmSheet = true
                        },
                        modifier =
                            Modifier
                                .padding(start = 15.dp, end = 15.dp)
                                .fillMaxWidth()
                                .showHandOnHover(),
                    ) {
                        Text(
                            text = "Add strings to dictionary",
                            style = MaterialTheme.typography.titleSmall,
                        )
                    }
                    AnimatedVisibility(
                        !isDictEmpty,
                        enter = fadeIn(),
                        exit = fadeOut(),
                    ) {
                        FilledTonalButton(
                            colors =
                                ButtonDefaults.filledTonalButtonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                                ),
                            onClick = {
                                showDeleteAllDictStringsDialogBox = true
                            },
                            modifier =
                                Modifier
                                    .padding(start = 15.dp, end = 15.dp, bottom = 15.dp)
                                    .fillMaxWidth()
                                    .showHandOnHover(),
                        ) {
                            Text(
                                text = "Delete all strings from dictionary",
                                style = MaterialTheme.typography.titleSmall,
                            )
                        }
                    }
                }
            }
            item {
                AnimatedVisibility(
                    dictionaryStrings.isNotEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    Text(
                        text = "Added Strings",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 15.dp, end = 15.dp),
                        fontSize = 16.sp,
                    )
                }
            }
            items(dictionaryStrings, key = {
                it.id
            }) { dictItem ->
                Row(
                    modifier =
                        Modifier
                            .padding(top = 7.5.dp, bottom = 7.5.dp)
                            .fillMaxWidth()
                            .animateItem(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = dictItem.string,
                        style = MaterialTheme.typography.titleSmall,
                        fontSize = 18.sp,
                        modifier =
                            Modifier
                                .padding(
                                    start = 15.dp,
                                    end = 15.dp,
                                ).fillMaxWidth(0.75f),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                    FilledIconButton(
                        modifier = Modifier.padding(end = 15.dp).showHandOnHover(),
                        onClick = {
                            settingsScreenVM.performAction(
                                SettingsScreenAction.DeleteItemFromDictionary(
                                    dictionaryItem = dictItem,
                                ),
                            )
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Delete,
                            contentDescription = "Deletes the string: ${dictItem.string} from your personal dictionary",
                        )
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
    AnimatedVisibility(showAccessibilityIconSizePreview, enter = fadeIn(), exit = fadeOut()) {
        Box(
            modifier =
                Modifier
                    .clickable(enabled = false, onClick = {})
                    .zIndex(100f)
                    .background(MaterialTheme.colorScheme.surface.copy(0.9f))
                    .fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(Res.drawable.secretary_bird_webp),
                contentDescription = null,
                modifier =
                    Modifier
                        .size(accessibilityIconSize)
                        .clip(CircleShape),
            )
        }
    }
    val coroutineScope = rememberCoroutineScope()
    val hideBtmSheet: () -> Unit = {
        coroutineScope
            .launch {
                newCustomStringInDictBtmSheet.hide()
            }.invokeOnCompletion {
                showNewCustomStringForDictBtmSheet = false
            }
    }

    if (showDeleteAllDictStringsDialogBox) {
        ConfirmationDialog(
            onDismissRequest = {
                showDeleteAllDictStringsDialogBox = false
            },
            onConfirm = {
                settingsScreenVM.performAction(
                    SettingsScreenAction.DeleteAllStringsFromDictionary(onCompletion = {
                        showDeleteAllDictStringsDialogBox = false
                    }),
                )
            },
            confirmText = "Delete All",
            title = "Do you really want to delete all the custom strings?",
        )
    }
    if (showDeleteAllDraftsDialogBox) {
        ConfirmationDialog(
            onDismissRequest = {
                showDeleteAllDraftsDialogBox = false
            },
            onConfirm = {
                settingsScreenVM.performAction(
                    SettingsScreenAction.DeleteAllDrafts(onCompletion = {
                        showDeleteAllDraftsDialogBox = false
                    }),
                )
            },
            confirmText = "Delete All",
            title = "Do you really want to delete all the drafts?",
        )
    }
    if (showImportExportProgressDialog) {
        BasicAlertDialog(
            onDismissRequest = {},
            modifier = Modifier,
            properties =
                DialogProperties(
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false,
                ),
            content = {
                Column(
                    modifier =
                        Modifier
                            .clip(AlertDialogDefaults.shape)
                            .background(AlertDialogDefaults.containerColor)
                            .padding(25.dp),
                ) {
                    Text(
                        text = importExportLabel,
                        style = MaterialTheme.typography.titleLarge,
                        fontSize = 26.sp,
                        color = AlertDialogDefaults.titleContentColor,
                    )
                    Spacer(Modifier.height(5.dp))
                    Text(
                        text = "This might take a moment. Hold on!",
                        style = MaterialTheme.typography.titleSmall,
                        fontSize = 18.sp,
                        color = AlertDialogDefaults.textContentColor,
                    )
                    Spacer(Modifier.height(15.dp))
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            },
        )
    }

    if (showNewCustomStringForDictBtmSheet) {
        var newString by rememberSaveable {
            mutableStateOf("")
        }
        ModalBottomSheet(
            modifier = Modifier.imePadding(),
            onDismissRequest = hideBtmSheet,
            sheetState = newCustomStringInDictBtmSheet,
        ) {
            Column(
                modifier =
                    Modifier
                        .padding(start = 15.dp, end = 15.dp, bottom = 15.dp)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .navigationBarsPadding(),
            ) {
                Text(
                    text = "Add strings to your dictionary",
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = 24.sp,
                )
                Spacer(Modifier.height(5.dp))
                Text(
                    text = "Every individual word must be in a new line.",
                    style = MaterialTheme.typography.titleSmall,
                    fontSize = 18.sp,
                )
                Spacer(Modifier.height(15.dp))
                TextField(
                    label = {
                        Text(
                            text = "Custom Strings",
                            style = MaterialTheme.typography.titleSmall,
                        )
                    },
                    textStyle = MaterialTheme.typography.titleSmall,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .focusRequester(newCustomStringInDictBtmSheetFocus),
                    value = newString,
                    onValueChange = { newValue ->
                        newString = newValue
                    },
                )
                Button(
                    modifier = Modifier.showHandOnHover().fillMaxWidth().padding(top = 15.dp),
                    onClick = {
                        settingsScreenVM.performAction(
                            settingsScreenAction =
                                SettingsScreenAction.AddStringsToDictionary(
                                    string = newString,
                                    onCompletion = hideBtmSheet,
                                ),
                        )
                    },
                ) {
                    Text(
                        text = "Add",
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
                OutlinedButton(
                    modifier = Modifier.showHandOnHover().fillMaxWidth(),
                    onClick = hideBtmSheet,
                ) {
                    Text(
                        text = "Cancel",
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
            }
        }
        LaunchedEffect(Unit) {
            newCustomStringInDictBtmSheetFocus.requestFocus()
        }
    }
}

@Composable
fun ItemDivider(
    colorOpacity: Float = 0.35f,
    thickness: Dp = 1.5.dp,
    paddingValues: PaddingValues =
        PaddingValues(
            top = 15.dp,
            start = 20.dp,
            end = if (platform.type == Platform.Type.AndroidMobile) 20.dp else 5.dp,
        ),
    color: Color = MaterialTheme.colorScheme.outline,
) {
    HorizontalDivider(
        modifier =
            Modifier
                .padding(
                    paddingValues,
                ).clip(RoundedCornerShape(25.dp)),
        thickness = thickness,
        color = color.copy(colorOpacity),
    )
}

@Composable
fun AppVersionLabel(modifier: Modifier = Modifier.padding(top = 7.5.dp, start = 15.dp)) {
    Row(modifier) {
        Text(
            text = "Limae",
            style = MaterialTheme.typography.labelSmall,
            fontSize = 22.sp,
            modifier = Modifier.alignByBaseline(),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        Text(
            text = Constants.APP_VERSION_NAME,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 12.5.sp,
            modifier = Modifier.alignByBaseline().padding(start = 2.5.dp),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}
