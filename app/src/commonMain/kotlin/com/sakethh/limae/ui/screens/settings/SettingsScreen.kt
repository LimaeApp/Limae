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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sakethh.limae.platform.Platform
import com.sakethh.limae.platform.platform
import com.sakethh.limae.ui.Icons
import com.sakethh.limae.ui.LimaeAction
import com.sakethh.limae.ui.common.ConfirmationDialog
import com.sakethh.limae.ui.common.ItemState
import com.sakethh.limae.ui.common.showHandOnHover
import com.sakethh.limae.utils.LimaePreferences
import kotlinx.coroutines.launch
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
    }) { paddingValues ->
        LazyColumn(
            contentPadding = paddingValues,
            modifier =
                Modifier
                    .fillMaxSize()
                    .nestedScroll(topAppBarScrollBehaviour.nestedScrollConnection),
        ) {
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
                        title = "Auto-save on writing",
                        doesDescriptionExists = true,
                        description = "Limae will auto-save your notes while you're writing with the empty interval of 0.5 seconds during input key strokes.",
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
                Spacer(modifier = Modifier.height(if (onAndroid) 7.5.dp else 15.dp))
            }
            if (onAndroid) {
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
                        text = "Limae will not show up nor process anything in the apps that are in the blocklist.",
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
                SettingComponent(
                    SettingComponentParam(
                        title = "Import",
                        doesDescriptionExists = true,
                        description = "Import from the JSON file which is based on Limae Schema.",
                        isSwitchNeeded = false,
                        isSwitchEnabled = LimaePreferences.autoSaveNotes,
                        onSwitchStateChange = {
                            settingsScreenVM.performAction(
                                SettingsScreenAction.ImportData(onCompletion = {
                                }),
                            )
                        },
                        showIcon = true,
                        icon = Icons.DataObject,
                        showFilledIcon = true,
                    ),
                )
                Spacer(modifier = Modifier.height(15.dp))
            }
            item {
                SettingComponent(
                    SettingComponentParam(
                        title = "Export",
                        doesDescriptionExists = true,
                        description = "Export Dictionary and Drafts to a JSON File.",
                        isSwitchNeeded = false,
                        isSwitchEnabled = LimaePreferences.autoSaveNotes,
                        onSwitchStateChange = {
                            settingsScreenVM.performAction(
                                SettingsScreenAction.ExportData(onCompletion = {
                                }),
                            )
                        },
                        showIcon = true,
                        icon = Icons.DataObject,
                        showFilledIcon = true,
                    ),
                )
                Spacer(modifier = Modifier.height(15.dp))
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
                Spacer(modifier = Modifier.height(15.dp))
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
                        text = "The dictionary includes your custom strings that will not be suggested for replacement by the engines used by Limae.",
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
                settingsScreenVM.performAction(SettingsScreenAction.DeleteAllStringsFromDictionary)
                showDeleteAllDictStringsDialogBox = false
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
                settingsScreenVM.performAction(SettingsScreenAction.DeleteAllDrafts)
                showDeleteAllDraftsDialogBox = false
            },
            confirmText = "Delete All",
            title = "Do you really want to delete all the drafts?",
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
