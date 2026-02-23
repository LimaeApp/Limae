package com.sakethh.limae.ui.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sakethh.limae.platform.Platform
import com.sakethh.limae.platform.platform
import com.sakethh.limae.ui.Icons
import com.sakethh.limae.ui.LimaeAction
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
    var showDeleteDialogBox by rememberSaveable {
        mutableStateOf(false)
    }
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
                                LimaePreferences.useSystemTheme =
                                    !LimaePreferences.useSystemTheme

                                settingsScreenVM.updatePreference(
                                    preferenceKey = booleanPreferencesKey(LimaePreferences.Key.USE_SYSTEM_THEME.name),
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
                                LimaePreferences.useDarkTheme =
                                    !LimaePreferences.useDarkTheme
                                settingsScreenVM.updatePreference(
                                    preferenceKey = booleanPreferencesKey(LimaePreferences.Key.USE_DARK_THEME.name),
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
                                LimaePreferences.useAmoledTheme =
                                    !LimaePreferences.useAmoledTheme
                                settingsScreenVM.updatePreference(
                                    preferenceKey = booleanPreferencesKey(LimaePreferences.Key.USE_AMOLED_THEME.name),
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
                                    preferenceKey = booleanPreferencesKey(LimaePreferences.Key.USE_DYNAMIC_THEME.name),
                                    newValue = LimaePreferences.useDynamicTheming,
                                )
                            },
                            showIcon = false,
                        ),
                    )
                    Spacer(modifier = Modifier.height(15.dp))
                }
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
                                top = if (onAndroid) 15.dp else 0.dp,
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
                                showDeleteDialogBox = true
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
                            Modifier.padding(
                                start = 15.dp,
                                end = 15.dp,
                            ),
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

    if (showDeleteDialogBox) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialogBox = false
            },
            confirmButton = {
                Button(
                    modifier = Modifier.showHandOnHover().fillMaxWidth(),
                    onClick = {
                        settingsScreenVM.performAction(SettingsScreenAction.DeleteAllStringsFromDictionary)
                        showDeleteDialogBox = false
                    },
                ) {
                    Text(
                        text = "Delete All",
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    modifier = Modifier.showHandOnHover().fillMaxWidth(),
                    onClick = { showDeleteDialogBox = false },
                ) {
                    Text(
                        text = "Cancel",
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
            },
            title = {
                Text(
                    text = "Do you really want to delete all the custom strings?",
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = 24.sp,
                )
            },
        )
    }

    if (showNewCustomStringForDictBtmSheet) {
        var newString by rememberSaveable {
            mutableStateOf("")
        }
        ModalBottomSheet(
            modifier =
                Modifier
                    .imePadding()
                    .navigationBarsPadding(),
            onDismissRequest = hideBtmSheet,
            sheetState = newCustomStringInDictBtmSheet,
        ) {
            Column(
                modifier =
                    Modifier
                        .padding(start = 15.dp, end = 15.dp, bottom = 15.dp)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
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
