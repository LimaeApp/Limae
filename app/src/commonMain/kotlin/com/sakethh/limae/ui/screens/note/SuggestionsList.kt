package com.sakethh.limae.ui.screens.note

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sakethh.limae.domain.model.LimaeSuggestionBundle
import com.sakethh.limae.platform.Platform
import com.sakethh.limae.platform.platform
import com.sakethh.limae.ui.Icons
import com.sakethh.limae.ui.common.SuggestionNote
import com.sakethh.limae.ui.common.showHandOnHover
import kotlinx.collections.immutable.PersistentList

typealias LimaeNotesIndex = Int
typealias SuggestionNoteIndex = Int

@Composable
fun SuggestionsList(
    suggestions: PersistentList<LimaeSuggestionBundle>,
    modifier: Modifier = Modifier.fillMaxSize(),
    showStickyHeader: Boolean = true,
    onAddToDictionary: (LimaeSuggestionBundle) -> Unit,
    onSuggestionAccept: (LimaeNotesIndex, SuggestionNoteIndex) -> Unit,
    onAcceptAll: () -> Unit,
) {
    LazyColumn(modifier = modifier) {
        if (showStickyHeader) {
            stickyHeader {
                Column(
                    modifier =
                        Modifier
                            .background(MaterialTheme.colorScheme.surface)
                            .fillMaxWidth(),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth().padding(15.dp),
                    ) {
                        Text(
                            text = "Suggestions",
                            style = MaterialTheme.typography.titleLarge,
                            fontSize = 24.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.fillMaxWidth(0.75f).padding(start = 5.dp),
                        )
                        FilledTonalIconButton(
                            enabled = !suggestions.isEmpty(),
                            modifier = Modifier.showHandOnHover(),
                            onClick = onAcceptAll,
                        ) {
                            Icon(
                                imageVector = Icons.DoneAll,
                                contentDescription = "Apply all the edits",
                            )
                        }
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(bottom = 5.dp).fillMaxWidth(),
                    )
                }
            }
        }
        item {
            Spacer(
                modifier =
                    Modifier.height(
                        if (platform.type == Platform.Type.AndroidMobile) 15.dp else 0.dp,
                    ),
            )
        }
        if (suggestions.isEmpty()) {
            item {
                Text(
                    modifier = Modifier.padding(15.dp),
                    text = "No suggestions yet.",
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 32.sp,
                )
            }
        }
        itemsIndexed(suggestions, key = { _, suggestion ->
            "Suggestion-${suggestion.suggestion.refId}"
        }) { index, suggestion ->
            SuggestionNote(
                limaeSuggestionBundle = suggestion,
                onAddToDictionary = {
                    onAddToDictionary(suggestion)
                },
                onSuggestionAccept = {
                    onSuggestionAccept(index, it)
                },
                modifier = Modifier.animateItem(),
            )
        }
        item {
            Spacer(
                modifier =
                    Modifier.height(
                        if (platform.type == Platform.Type.AndroidMobile) 15.dp else 250.dp,
                    ),
            )
        }
    }
}
