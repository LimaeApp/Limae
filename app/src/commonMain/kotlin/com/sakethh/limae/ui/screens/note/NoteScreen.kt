package com.sakethh.limae.ui.screens.note

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.sakethh.limae.data.repository.SuggestionCheckRepoImpl
import com.sakethh.limae.model.LimaeNote
import com.sakethh.limae.platform.HarperEngine
import com.sakethh.limae.ui.Icons
import com.sakethh.limae.ui.LimaeAction
import com.sakethh.limae.ui.common.SuggestionNote
import com.sakethh.limae.ui.common.showHandOnHover
import kotlinx.collections.immutable.PersistentList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteScreen(
    takeAction: (LimaeAction) -> Unit,
    title: String,
    content: String,
    lastSavedOn: String
) {
    val noteScreenVM: NoteScreenVM = viewModel(factory = viewModelFactory {
        initializer {
            NoteScreenVM(
                suggestionCheckRepo = SuggestionCheckRepoImpl(
                    harperEngine = HarperEngine
                ), title = title,
                content = content
            )
        }
    })
    val suggestions by noteScreenVM.suggestions.collectAsStateWithLifecycle()
    val topAppBarScrollBehaviour = TopAppBarDefaults.pinnedScrollBehavior()
    val textFieldColors = TextFieldDefaults.colors(
        focusedContainerColor = Color.Transparent,
        errorContainerColor = Color.Transparent,
        disabledContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        errorIndicatorColor = Color.Transparent,
        focusedIndicatorColor = Color.Transparent,
        disabledIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
    )
    Scaffold(modifier = Modifier.fillMaxSize(), topBar = {
        Column {
            TopAppBar(scrollBehavior = topAppBarScrollBehaviour, title = {
                Text(
                    text = "Limae",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 18.sp
                )
            }, navigationIcon = {
                IconButton(modifier = Modifier.showHandOnHover(), onClick = {
                    takeAction(LimaeAction.NavigateBack)
                }) {
                    Icon(
                        imageVector = Icons.ArrowBack,
                        contentDescription = "Icon button to navigate back to main screen"
                    )
                }
            })
            HorizontalDivider()
        }
    }) { paddingValues ->
        Row(
            modifier = Modifier.padding(paddingValues)
                .nestedScroll(topAppBarScrollBehaviour.nestedScrollConnection).fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.65f)
            ) {
                item {
                    TextField(
                        placeholder = {
                            Text(
                                text = "Title",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        },
                        value = noteScreenVM.noteTitle,
                        onValueChange = {
                            noteScreenVM.onAction(NoteScreenAction.OnTitleChange(it))
                        },
                        textStyle = MaterialTheme.typography.titleMedium.copy(fontSize = 24.sp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors
                    )
                }
                item {
                    TextField(
                        placeholder = {
                            Text(
                                text = "Content",
                                fontSize = 18.sp,
                                style = MaterialTheme.typography.titleSmall
                            )
                        },
                        value = noteScreenVM.noteContent,
                        onValueChange = {
                            noteScreenVM.onAction(NoteScreenAction.OnContentChange(it))
                        },
                        textStyle = MaterialTheme.typography.titleSmall.copy(
                            fontSize = 18.sp,
                        ),
                        modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 250.dp),
                        colors = textFieldColors
                    )
                }
                item {
                    Text(
                        text = "Last saved on $lastSavedOn",
                        modifier = Modifier.padding(start = 15.dp)
                            .imePadding(),
                        color = MaterialTheme.colorScheme.secondary,
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }
            VerticalDivider(
                modifier = Modifier.fillMaxHeight().padding(start = 7.5.dp)
            )
            SuggestionsList(suggestions.data)
        }
    }
}

@Composable
fun SuggestionsList(suggestions: PersistentList<LimaeNote>) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        stickyHeader {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                Text(
                    text = "Suggestions",
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 15.dp, top = 15.dp)
                )
                HorizontalDivider(
                    modifier = Modifier.padding(top = 15.dp, bottom = 5.dp).fillMaxWidth(),
                )
            }
        }
        if (suggestions.isEmpty()) {
            item {
                Text(
                    modifier = Modifier.padding(15.dp),
                    text = "It's all empty here.",
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 32.sp
                )
            }
        }
        items(suggestions) {
            SuggestionNote(it)
        }
        item {
            Spacer(modifier = Modifier.height(250.dp))
        }
    }
}