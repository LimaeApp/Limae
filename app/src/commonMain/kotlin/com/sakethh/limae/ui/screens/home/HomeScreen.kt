@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.sakethh.limae.ui.screens.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sakethh.limae.ui.Icons
import com.sakethh.limae.ui.LimaeAction
import com.sakethh.limae.ui.common.showHandOnHover
import com.sakethh.limae.ui.navigation.NavRoute
import com.sakethh.limae.utils.epochToReadableDateTime
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    takeAction: (LimaeAction) -> Unit,
    FABHeight: (Dp) -> Unit,
) {
    val topAppBarScrollBehaviour = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val homeScreenVM: HomeScreenVM = koinViewModel()

    val savedNotes by homeScreenVM.savedNotes.collectAsStateWithLifecycle()

    var showSearchBar by rememberSaveable {
        mutableStateOf(false)
    }

    val isReadTextFieldAccessibilityServiceRunning by homeScreenVM.isReadTextFieldServiceRunning.collectAsStateWithLifecycle()

    val searchBarFocusRequester =
        retain {
            FocusRequester()
        }

    LaunchedEffect(showSearchBar) {
        if (showSearchBar) {
            delay(150L)
            searchBarFocusRequester.requestFocus()
        }
    }

    val accessibilityServiceNoticeBtmSheet =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
            confirmValueChange = { sheetValue ->
                if (sheetValue == SheetValue.Hidden) {
                    isReadTextFieldAccessibilityServiceRunning
                } else {
                    true
                }
            },
        )

    val localDensity = LocalDensity.current

    Scaffold(topBar = {
        LargeTopAppBar(title = {
            Text(
                text = "Limae",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.primary,
            )
        }, scrollBehavior = topAppBarScrollBehaviour)
    }, floatingActionButton = {
        Row(
            modifier =
                Modifier
                    .onGloballyPositioned {
                        with(localDensity) {
                            FABHeight(it.size.height.toDp())
                        }
                    }.clip(RoundedCornerShape(50.dp))
                    .background(
                        FloatingActionButtonDefaults.containerColor,
                    ).padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AnimatedContent(showSearchBar) { _showSearchBar ->
                if (_showSearchBar) {
                    OutlinedTextField(
                        shape = RoundedCornerShape(50.dp),
                        modifier =
                            Modifier
                                .fillMaxWidth(0.625f)
                                .padding(start = 2.5.dp)
                                .focusRequester(searchBarFocusRequester),
                        textStyle = MaterialTheme.typography.titleSmall,
                        placeholder = {
                            Text(
                                text = "Search for title/content of your drafts",
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.basicMarquee(),
                            )
                        },
                        value = homeScreenVM.searchQuery,
                        onValueChange = {
                            homeScreenVM.performAction(HomeScreenAction.UpdateSearchQuery(string = it))
                        },
                        trailingIcon = {
                            IconButton(modifier = Modifier.showHandOnHover(), onClick = {
                                if (homeScreenVM.searchQuery.isNotBlank()) {
                                    homeScreenVM.performAction(
                                        HomeScreenAction.UpdateSearchQuery(
                                            string = "",
                                        ),
                                    )
                                } else {
                                    showSearchBar = false
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Close,
                                    contentDescription = "Close Icon button to close the search bar",
                                )
                            }
                        },
                    )
                } else {
                    IconButton(modifier = Modifier.showHandOnHover(), onClick = {
                        showSearchBar = true
                    }) {
                        Icon(
                            imageVector = Icons.Search,
                            contentDescription = "Search Icon button to open the search bar",
                        )
                    }
                }
            }
            IconButton(modifier = Modifier.showHandOnHover(), onClick = {
                takeAction(LimaeAction.Navigate(destination = NavRoute.Settings))
            }) {
                Icon(
                    imageVector = Icons.Settings,
                    contentDescription = "Settings Icon button to navigate to the settings screen",
                )
            }
            FilledIconButton(modifier = Modifier.showHandOnHover(), onClick = {
                takeAction(LimaeAction.Navigate(destination = NavRoute.Note(noteId = null)))
            }) {
                Icon(
                    imageVector = Icons.AddNotes,
                    contentDescription = "Add-Notes Icon button to open to add a new note",
                )
            }
        }
    }) { paddingValues ->
        LazyVerticalStaggeredGrid(
            modifier =
                Modifier
                    .fillMaxSize()
                    .nestedScroll(topAppBarScrollBehaviour.nestedScrollConnection),
            contentPadding = paddingValues,
            columns = StaggeredGridCells.Adaptive(250.dp),
        ) {
            item(span = StaggeredGridItemSpan.FullLine) {
                Text(
                    text = "Drafts",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 15.dp, top = 15.dp, bottom = 7.5.dp),
                    fontSize = 20.sp,
                    textAlign = TextAlign.Start,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
            item(span = StaggeredGridItemSpan.FullLine) {
                AnimatedContent(savedNotes.isLoading to savedNotes.data.isEmpty()) { (isLoading, isDataEmpty) ->
                    if (isLoading) {
                        Box(
                            modifier = Modifier.padding(top = 100.dp).fillMaxWidth(),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularWavyProgressIndicator()
                        }
                    } else if (isDataEmpty) {
                        AnimatedContent(targetState = showSearchBar && homeScreenVM.searchQuery.isNotBlank()) { searchResultsEmpty ->
                            if (searchResultsEmpty) {
                                Text(
                                    text = "No results found.",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(top = 75.dp, start = 15.dp),
                                    fontSize = 18.sp,
                                    textAlign = TextAlign.Start,
                                    color = MaterialTheme.colorScheme.tertiary,
                                )
                            } else {
                                Column {
                                    Text(
                                        text = "No drafts yet. Write something and it'll show up here.",
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier =
                                            Modifier.padding(
                                                top = 75.dp,
                                                start = 15.dp,
                                                end = 25.dp,
                                            ),
                                        fontSize = 18.sp,
                                        textAlign = TextAlign.Start,
                                        color = MaterialTheme.colorScheme.tertiary,
                                    )

                                    Text(
                                        inlineContent =
                                            mapOf(
                                                "AddNoteIcon" to
                                                    InlineTextContent(
                                                        placeholder =
                                                            Placeholder(
                                                                width = 36.sp,
                                                                height = 36.sp,
                                                                placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter,
                                                            ),
                                                        children = {
                                                            FilledIconButton(
                                                                modifier = Modifier.showHandOnHover(),
                                                                colors =
                                                                    IconButtonDefaults.iconButtonColors(
                                                                        contentColor = MaterialTheme.colorScheme.onTertiary,
                                                                        containerColor = MaterialTheme.colorScheme.tertiary,
                                                                    ),
                                                                onClick = {
                                                                    takeAction(
                                                                        LimaeAction.Navigate(
                                                                            NavRoute.Note(
                                                                                noteId = null,
                                                                            ),
                                                                        ),
                                                                    )
                                                                },
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.AddNotes,
                                                                    contentDescription = "Add Notes Icon",
                                                                )
                                                            }
                                                        },
                                                    ),
                                            ),
                                        text =
                                            buildAnnotatedString {
                                                append("Click ")
                                                appendInlineContent(id = "AddNoteIcon")
                                                append(" to create a new draft.")
                                            },
                                        style = MaterialTheme.typography.titleSmall,
                                        modifier = Modifier.padding(top = 15.dp, start = 15.dp),
                                        fontSize = 18.sp,
                                        textAlign = TextAlign.Start,
                                        color = MaterialTheme.colorScheme.tertiary,
                                    )
                                }
                            }
                        }
                    }
                }
            }
            items(savedNotes.data, key = {
                it.id
            }) { note ->
                Card(
                    modifier =
                        Modifier
                            .padding(start = 15.dp, end = 15.dp, top = 7.5.dp, bottom = 7.5.dp)
                            .clickable(indication = null, interactionSource = null) {
                                takeAction(
                                    LimaeAction.Navigate(
                                        destination =
                                            NavRoute.Note(
                                                noteId = note.id,
                                            ),
                                    ),
                                )
                            }.showHandOnHover()
                            .animateItem(),
                ) {
                    Row(
                        modifier = Modifier.padding(top = 15.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.fillMaxWidth(0.7f)) {
                            Text(
                                text = note.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontSize = 18.sp,
                                modifier =
                                    Modifier.padding(
                                        start = 15.dp,
                                        end = 15.dp,
                                    ),
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = note.content,
                                style = MaterialTheme.typography.titleSmall,
                                fontSize = 16.sp,
                                modifier =
                                    Modifier.padding(
                                        start = 15.dp,
                                        end = 15.dp,
                                    ),
                                maxLines = 5,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.secondary,
                            )
                        }
                        FilledIconButton(modifier = Modifier.padding(end = 15.dp), onClick = {
                            homeScreenVM.performAction(
                                HomeScreenAction.DeleteANote(
                                    noteId = note.id,
                                    onCompletion = {},
                                ),
                            )
                        }) {
                            Icon(
                                imageVector = Icons.Delete,
                                contentDescription = "Deletes the draft titled: ${note.title}",
                            )
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(15.dp).fillMaxWidth())
                    Text(
                        text =
                            rememberSaveable(note.lastModified) {
                                epochToReadableDateTime(note.lastModified).toString()
                            },
                        style = MaterialTheme.typography.titleSmall,
                        fontSize = 14.sp,
                        modifier =
                            Modifier.padding(
                                start = 15.dp,
                                bottom = 15.dp,
                                end = 15.dp,
                            ),
                        maxLines = 5,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.secondary.copy(0.75f),
                    )
                }
            }
        }
    }

    if (false && !isReadTextFieldAccessibilityServiceRunning) {
        ModalBottomSheet(
            onDismissRequest = {},
            sheetState = accessibilityServiceNoticeBtmSheet,
        ) {
            Column(
                modifier =
                    Modifier
                        .padding(start = 15.dp, end = 15.dp)
                        .fillMaxWidth()
                        .navigationBarsPadding(),
            ) {
                Text(
                    text = "Accessibility Permission Required",
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(5.dp))
                Text(
                    text = "Limae requires the accessibility permission to work.\nEverything is processed locally, your drafts and any other information remains on your device.",
                    style = MaterialTheme.typography.titleSmall,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.secondary,
                )
                Spacer(Modifier.height(15.dp))
                Text(
                    text = "1. Tap \"Open Accessibility Settings\"\n2. Go to \"Downloaded Apps\"\n3. Select \"Limae\"\n4. Enable the \"Limae\" toggle\n5. Grant \"Full control of your device\"\n   when prompted",
                    style = MaterialTheme.typography.titleSmall,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.secondary.copy(0.75f),
                )
                Button(
                    modifier = Modifier.showHandOnHover().fillMaxWidth().padding(top = 15.dp),
                    onClick = {
                        homeScreenVM.performAction(HomeScreenAction.OpenAccessibilityServiceScreen)
                    },
                ) {
                    Text(
                        text = "Open Accessibility Settings",
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
            }
        }
    }
}
