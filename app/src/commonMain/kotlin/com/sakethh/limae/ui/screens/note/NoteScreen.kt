package com.sakethh.limae.ui.screens.note

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.sakethh.limae.platform.Platform
import com.sakethh.limae.platform.isReadTextFieldAccessibilityServiceRunning
import com.sakethh.limae.platform.platform
import com.sakethh.limae.ui.Icons
import com.sakethh.limae.ui.LimaeAction
import com.sakethh.limae.ui.common.AccessibilitySuggestionsSheet
import com.sakethh.limae.ui.common.showHandOnHover
import com.sakethh.limae.utils.Constants
import com.sakethh.limae.utils.LimaePreferences
import com.sakethh.limae.utils.addEdgeToEdgeScaffoldPadding
import com.sakethh.limae.utils.epochToReadableDateTime
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import limae.app.generated.resources.Res
import limae.app.generated.resources.secretary_bird
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.getKoin
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteScreen(
    takeAction: (LimaeAction) -> Unit,
    noteId: String?,
    showAccessibilityOverlay: Boolean,
) {
    val koinInstance = getKoin()
    var currentWidthOfWindow by retain {
        mutableIntStateOf(0)
    }

    val registerListeningToSuggestions =
        retain {
            derivedStateOf {
                platform.type != Platform.Type.AndroidMobile || showAccessibilityOverlay
            }
        }

    val noteScreenVM: NoteScreenVM =
        viewModel(
            factory =
                viewModelFactory {
                    initializer {
                        NoteScreenVM(
                            suggestionsRepo = koinInstance.get(),
                            sourceNoteId = noteId,
                            notesRepo = koinInstance.get(),
                            registerListeningToSuggestions = registerListeningToSuggestions,
                        )
                    }
                },
        )
    val suggestions by noteScreenVM.suggestions.collectAsStateWithLifecycle()
    val suggestionsContent: @Composable (Modifier) -> Unit = { modifier ->
        SuggestionsList(
            modifier = modifier,
            onAcceptAll = {
                noteScreenVM.onAction(
                    noteScreenAction = NoteScreenAction.AcceptAllSuggestions,
                )
            },
            suggestions = suggestions.data,
            onAddToDictionary = {
                noteScreenVM.onAction(
                    NoteScreenAction.AddStringToDictionary(it.suggestion.errorSequence),
                )
            },
            onSuggestionAccept = { limaeNotesIndex, suggestionNoteIndex ->
                noteScreenVM.onAction(
                    NoteScreenAction.OnSuggestionAccept(
                        limaeNotesIndex,
                        suggestionNoteIndex,
                    ),
                )
            },
        )
    }
    val topAppBarScrollBehaviour = TopAppBarDefaults.pinnedScrollBehavior()
    val showSuggestionsPanelInBottom by retain {
        derivedStateOf {
            platform.type != Platform.Type.AndroidMobile && currentWidthOfWindow < Constants.MIN_WIDTH_FOR_SUGGESTION_PANEL_ON_RIGHT
        }
    }
    val showSuggestionsPanelOnRight by retain {
        derivedStateOf {
            platform.type != Platform.Type.AndroidMobile && currentWidthOfWindow >= Constants.MIN_WIDTH_FOR_SUGGESTION_PANEL_ON_RIGHT
        }
    }
    val textFieldColors =
        TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            errorContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        )

    var showSavingLabel by rememberSaveable {
        mutableStateOf(false)
    }

    val localWindowInfo = LocalWindowInfo.current
    val localDensity = LocalDensity.current
    var accessibilityIconOffset by
        retain {
            mutableStateOf(Offset(45f, 254f))
        }
    val accessibilityIconSizePx =
        with(localDensity) { LimaePreferences.accessibilityIconSize.dp.toPx() }
    LaunchedEffect(Unit) {
        snapshotFlow {
            noteScreenVM.isSavingANote
        }.drop(1).collectLatest {
            if (showSavingLabel && !noteScreenVM.isSavingANote && noteScreenVM.note.title.isBlank() &&
                noteScreenVM.note.content.isBlank()
            ) {
                return@collectLatest
            }

            showSavingLabel = true
            delay(1500L)
            showSavingLabel = false
        }
    }
    var maxViewportOffset by retain { mutableStateOf(Offset.Zero) }
    val isReadTextFieldAccessibilityServiceRunning by isReadTextFieldAccessibilityServiceRunning.collectAsStateWithLifecycle(
        initialValue = !showAccessibilityOverlay,
    )
    var showAccessibilitySuggestions by rememberSaveable {
        mutableStateOf(false)
    }
    Box(
        modifier =
            Modifier.fillMaxSize().onSizeChanged { size ->
                maxViewportOffset =
                    Offset(
                        x = size.width.toFloat(),
                        y = size.height.toFloat(),
                    )
            },
    ) {
        AnimatedVisibility(
            visible = !showAccessibilitySuggestions && !isReadTextFieldAccessibilityServiceRunning,
            modifier =
                Modifier
                    .offset {
                        IntOffset(
                            accessibilityIconOffset.x.roundToInt(),
                            accessibilityIconOffset.y.roundToInt(),
                        )
                    }.pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            accessibilityIconOffset =
                                accessibilityIconOffset.copy(
                                    x =
                                        (accessibilityIconOffset.x + dragAmount.x)
                                            .coerceAtLeast(0f)
                                            .coerceAtMost(maxViewportOffset.x - accessibilityIconSizePx),
                                    y =
                                        (accessibilityIconOffset.y + dragAmount.y)
                                            .coerceAtLeast(0f)
                                            .coerceAtMost(maxViewportOffset.y - accessibilityIconSizePx),
                                )
                        }
                    }.zIndex(100f)
                    .size(LimaePreferences.accessibilityIconSize.dp)
                    .clip(CircleShape)
                    .clickable {
                        showAccessibilitySuggestions = true
                    },
        ) {
            Image(
                painter = painterResource(Res.drawable.secretary_bird),
                contentDescription = "Expands the Limae Interface",
            )
        }

        AnimatedVisibility(
            visible = showAccessibilitySuggestions,
            modifier = Modifier.zIndex(150f),
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                AccessibilitySuggestionsSheet(
                    foregroundApp = null,
                    onDismissRequest = { showAccessibilitySuggestions = false },
                    forNotesScreen = true,
                    onBlockRequest = {},
                    suggestions = suggestions.data,
                    onAddToDictionary = {
                        noteScreenVM.onAction(
                            NoteScreenAction.AddStringToDictionary(it.suggestion.errorSequence),
                        )
                    },
                    onSuggestionAccept = { limaeNotesIndex, suggestionNoteIndex ->
                        noteScreenVM.onAction(
                            NoteScreenAction.OnSuggestionAccept(
                                limaeNotesIndex,
                                suggestionNoteIndex,
                            ),
                        )
                    },
                )
            }
        }

        Scaffold(modifier = Modifier.fillMaxSize(), topBar = {
            Column(
                modifier =
                    Modifier.fillMaxWidth().onGloballyPositioned {
                        currentWidthOfWindow = it.size.width
                    },
            ) {
                TopAppBar(scrollBehavior = topAppBarScrollBehaviour, title = {
                    Text(
                        text = "Limae",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 18.sp,
                    )
                }, navigationIcon = {
                    if (platform.type == Platform.Type.Web) return@TopAppBar

                    IconButton(modifier = Modifier.showHandOnHover(), onClick = {
                        takeAction(LimaeAction.NavigateBack)
                    }) {
                        Icon(
                            imageVector = Icons.ArrowBack,
                            contentDescription = "Icon button to navigate back to main screen",
                        )
                    }
                }, actions = {
                    if (platform.type == Platform.Type.Web) return@TopAppBar

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AnimatedVisibility(
                            visible = showSavingLabel,
                            enter = fadeIn(),
                            exit = fadeOut(),
                        ) {
                            Text(
                                text = "Saving...",
                                style = MaterialTheme.typography.titleMedium,
                                fontSize = 12.5.sp,
                                fontStyle = FontStyle.Italic,
                                modifier = Modifier.padding(end = 5.dp),
                                color = MaterialTheme.colorScheme.secondary.copy(0.75f),
                            )
                        }
                        Box(contentAlignment = Alignment.Center) {
                            with(this@TopAppBar) {
                                AnimatedVisibility(
                                    visible =
                                        noteScreenVM.isSavingANote &&
                                            (
                                                noteScreenVM.note.title.isNotBlank() ||
                                                    noteScreenVM.note.content.isNotBlank()
                                            ),
                                    enter = fadeIn(),
                                    exit = fadeOut(),
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                            IconButton(modifier = Modifier.showHandOnHover(), onClick = {
                                noteScreenVM.onAction(
                                    NoteScreenAction.SaveNote(
                                        noteId = noteId,
                                        title = noteScreenVM.note.title,
                                        content = noteScreenVM.note.content,
                                        onCompletion = {},
                                    ),
                                )
                            }) {
                                Icon(
                                    imageVector = Icons.Save,
                                    contentDescription = "Updates/Saves the title and content to the local database",
                                )
                            }
                        }
                    }
                })
                if (platform.type != Platform.Type.AndroidMobile) {
                    HorizontalDivider()
                }
            }
        }) { paddingValues ->
            Box(modifier = Modifier.addEdgeToEdgeScaffoldPadding(paddingValues).fillMaxSize()) {
                Column(modifier = Modifier.align(Alignment.BottomCenter)) {
                    AnimatedVisibility(showSuggestionsPanelInBottom) {
                        Column {
                            HorizontalDivider(
                                modifier = Modifier.fillMaxWidth(),
                                thickness = 2.5.dp,
                            )
                            suggestionsContent(Modifier.fillMaxWidth().heightIn(max = 300.dp))
                        }
                    }
                }
                Row(
                    modifier =
                        Modifier
                            .nestedScroll(topAppBarScrollBehaviour.nestedScrollConnection)
                            .fillMaxSize(),
                ) {
                    LazyColumn(
                        modifier =
                            Modifier
                                .animateContentSize()
                                .padding(
                                    PaddingValues(
                                        bottom = if (showSuggestionsPanelInBottom) 300.dp else 0.dp,
                                    ),
                                ).fillMaxHeight()
                                .fillMaxWidth(
                                    if (platform.type == Platform.Type.AndroidMobile ||
                                        showSuggestionsPanelInBottom
                                    ) {
                                        1f
                                    } else {
                                        0.65f
                                    },
                                ),
                    ) {
                        item {
                            TextField(
                                placeholder = {
                                    Text(
                                        text = "Title",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        style = MaterialTheme.typography.titleMedium,
                                    )
                                },
                                value = noteScreenVM.note.title,
                                onValueChange = {
                                    noteScreenVM.onAction(NoteScreenAction.OnTitleChange(it))
                                },
                                textStyle = MaterialTheme.typography.titleMedium.copy(fontSize = 24.sp),
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = localWindowInfo.containerSize.height.dp)
                                        .onFocusChanged {
                                            if (it.isFocused) {
                                                noteScreenVM.onAction(
                                                    NoteScreenAction.UpdateFocusedTextField(
                                                        field = ActiveTextField.Title,
                                                    ),
                                                )
                                            }
                                        },
                                /*.padding(15.dp)
                                .clip(RoundedCornerShape(25.dp))
                                .border(
                                    width = 1.5.dp,
                                    color = MaterialTheme.colorScheme.primary.copy(0.15f),
                                    shape = RoundedCornerShape(25.dp),
                                )*/
                                colors = textFieldColors,
                            )
                        }
                        item {
                            TextField(
                                placeholder = {
                                    Text(
                                        text = "Content",
                                        fontSize = 18.sp,
                                        style = MaterialTheme.typography.titleSmall,
                                    )
                                },
                                value = noteScreenVM.note.content,
                                onValueChange = {
                                    noteScreenVM.onAction(NoteScreenAction.OnContentChange(it))
                                },
                                textStyle =
                                    MaterialTheme.typography.titleSmall.copy(
                                        fontSize = 18.sp,
                                    ),
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .defaultMinSize(minHeight = 250.dp)
                                        .heightIn(max = localWindowInfo.containerSize.height.dp)
                                        .onFocusChanged {
                                            if (it.isFocused) {
                                                noteScreenVM.onAction(
                                                    NoteScreenAction.UpdateFocusedTextField(
                                                        field = ActiveTextField.Content,
                                                    ),
                                                )
                                            }
                                        },
                                /*.padding(15.dp)
                                .clip(RoundedCornerShape(25.dp))
                                .border(
                                    width = 1.5.dp,
                                    color = MaterialTheme.colorScheme.primary.copy(0.15f),
                                    shape = RoundedCornerShape(25.dp),
                                )*/
                                colors = textFieldColors,
                            )
                        }

                        if (platform.type == Platform.Type.Web) return@LazyColumn
                        item {
                            AnimatedVisibility(
                                noteScreenVM.note.id != "",
                                enter = fadeIn(),
                                exit = fadeOut(),
                            ) {
                                Text(
                                    text =
                                        rememberSaveable(noteScreenVM.note.lastModified) {
                                            "Last saved on ${epochToReadableDateTime(noteScreenVM.note.lastModified)}"
                                        },
                                    modifier =
                                        Modifier
                                            .padding(start = 15.dp, bottom = 15.dp)
                                            .imePadding(),
                                    color = MaterialTheme.colorScheme.secondary,
                                    style = MaterialTheme.typography.titleSmall,
                                )
                            }
                        }
                    }
                    AnimatedVisibility(showSuggestionsPanelOnRight) {
                        Row(
                            modifier =
                                Modifier
                                    .background(MaterialTheme.colorScheme.surface)
                                    .fillMaxSize(),
                        ) {
                            VerticalDivider(
                                modifier = Modifier.fillMaxHeight(),
                            )
                            suggestionsContent(Modifier.fillMaxSize())
                        }
                    }
                }
            }
        }
    }
}
