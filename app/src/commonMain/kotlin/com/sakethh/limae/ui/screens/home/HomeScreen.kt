package com.sakethh.limae.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sakethh.limae.ui.Icons
import com.sakethh.limae.ui.LimaeAction
import com.sakethh.limae.ui.common.showHandOnHover
import com.sakethh.limae.ui.navigation.NavRoute
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(takeAction: (LimaeAction) -> Unit) {
    val topAppBarScrollBehaviour = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val homeScreenVM: HomeScreenVM = koinViewModel()

    val savedNotes by homeScreenVM.savedNotes.collectAsStateWithLifecycle()

    Scaffold(topBar = {
        LargeTopAppBar(title = {
            Text(
                text = "Limae",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }, scrollBehavior = topAppBarScrollBehaviour)
    }, floatingActionButton = {
        Row(
            modifier = Modifier.clip(RoundedCornerShape(50.dp)).background(
                FloatingActionButtonDefaults.containerColor
            ).padding(10.dp)
        ) {
            IconButton(modifier = Modifier.showHandOnHover(), onClick = {}) {
                Icon(
                    imageVector = Icons.Search,
                    contentDescription = "Search Icon button to open the search bar"
                )
            }
            IconButton(modifier = Modifier.showHandOnHover(), onClick = {}) {
                Icon(
                    imageVector = Icons.Settings,
                    contentDescription = "Settings Icon button to navigate to the settings screen"
                )
            }
            FilledIconButton(modifier = Modifier.showHandOnHover(), onClick = {
                takeAction(LimaeAction.Navigate(destination = NavRoute.Note(noteId = null)))
            }) {
                Icon(
                    imageVector = Icons.AddNotes,
                    contentDescription = "Add-Notes Icon button to open to add a new note"
                )
            }
        }
    }) { paddingValues ->
        LazyVerticalStaggeredGrid(
            modifier = Modifier.fillMaxSize()
                .nestedScroll(topAppBarScrollBehaviour.nestedScrollConnection),
            contentPadding = paddingValues,
            columns = StaggeredGridCells.Adaptive(250.dp)
        ) {
            item(span = StaggeredGridItemSpan.FullLine) {
                Text(
                    text = "Drafts",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 15.dp, top = 15.dp),
                    fontSize = 20.sp,
                    textAlign = TextAlign.Start,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            items(savedNotes) { note ->
                Card(
                    modifier = Modifier.padding(15.dp)
                        .clickable(indication = null, interactionSource = null) {
                            takeAction(LimaeAction.Navigate(destination = NavRoute.Note(noteId = note.id)))
                        }.showHandOnHover()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.fillMaxWidth(0.7f)) {
                            Text(
                                text = note.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontSize = 18.sp,
                                modifier = Modifier.padding(
                                    start = 15.dp,
                                    end = 15.dp,
                                    top = 15.dp,
                                    bottom = 5.dp
                                ),
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = note.content,
                                style = MaterialTheme.typography.titleSmall,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(
                                    start = 15.dp,
                                    bottom = 15.dp,
                                    end = 15.dp
                                ),
                                maxLines = 5,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        FilledIconButton(modifier = Modifier.padding(end = 15.dp), onClick = {
                            homeScreenVM.performAction(
                                HomeScreenAction.DeleteANote(
                                    noteId = note.id,
                                    onCompletion = {})
                            )
                        }) {
                            Icon(
                                imageVector = Icons.Delete,
                                contentDescription = "Deletes the draft titled: ${note.title}"
                            )
                        }
                    }
                }
            }
        }
    }
}