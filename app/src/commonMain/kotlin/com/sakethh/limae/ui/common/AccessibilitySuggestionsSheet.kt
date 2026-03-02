package com.sakethh.limae.ui.common

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sakethh.limae.domain.model.LimaeSuggestionBundle
import com.sakethh.limae.platform.Platform
import com.sakethh.limae.ui.Icons
import com.sakethh.limae.ui.screens.note.LimaeNotesIndex
import com.sakethh.limae.ui.screens.note.SuggestionNoteIndex
import com.sakethh.limae.ui.screens.note.SuggestionsList
import com.sakethh.limae.ui.theme.LimaeTheme
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccessibilitySuggestionsSheet(
    foregroundApp: Platform.Actions.InstalledApp?,
    onDismissRequest: () -> Unit,
    forNotesScreen: Boolean = false,
    onBlockRequest: () -> Unit,
    suggestions: PersistentList<LimaeSuggestionBundle>,
    onAddToDictionary: (LimaeSuggestionBundle) -> Unit,
    onSuggestionAccept: (LimaeNotesIndex, SuggestionNoteIndex) -> Unit,
) {
    Surface {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 150.dp, max = 500.dp)
                    .background(MaterialTheme.colorScheme.surface),
        ) {
            Column(
                modifier =
                    Modifier
                        .animateContentSize()
                        .fillMaxWidth(),
            ) {
                HorizontalDivider(thickness = 2.5.dp)
                Row(
                    modifier =
                        Modifier
                            .padding(
                                start = 10.dp,
                                end = 10.dp,
                                top = if (!forNotesScreen) 10.dp else 0.dp,
                            ).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.fillMaxWidth(0.45f)) {
                        Text(
                            text = "Limae",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 22.sp,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        AnimatedContent(foregroundApp) { foregroundApp ->
                            Column {
                                if (foregroundApp != null) {
                                    Text(
                                        text = foregroundApp.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.secondary.copy(0.95f),
                                    )
                                }
                            }
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!forNotesScreen) {
                            TextButton(onClick = onBlockRequest) {
                                Icon(
                                    imageVector = Icons.Block,
                                    contentDescription = "Block Limae's processing when using the app that's currently being used.",
                                )
                                Spacer(Modifier.width(5.dp))
                                Text(
                                    text = "Block",
                                    style = MaterialTheme.typography.titleSmall,
                                )
                            }
                            Spacer(Modifier.width(5.dp))
                        }
                        ElevatedButton(onClick = onDismissRequest) {
                            Icon(
                                imageVector = Icons.Close,
                                contentDescription = "Minimize Limae's correction notes bottom sheet.",
                            )
                        }
                    }
                }
                AnimatedContent(foregroundApp) { foregroundApp ->
                    if (foregroundApp != null) {
                        Text(
                            text = foregroundApp.packageName,
                            style = MaterialTheme.typography.titleSmall,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.secondary.copy(0.9f),
                            modifier = Modifier.padding(start = 10.dp, end = 10.dp, bottom = 10.dp),
                        )
                    }
                }
                HorizontalDivider(thickness = 2.5.dp)
                SuggestionsList(
                    suggestions = suggestions,
                    modifier = Modifier.fillMaxWidth(),
                    showStickyHeader = false,
                    onAddToDictionary = onAddToDictionary,
                    onSuggestionAccept = onSuggestionAccept,
                    onAcceptAll = {},
                )
            }
        }
    }
}

@Composable
@Preview
private fun AccessibilitySuggestionsSheetPreview() {
    LimaeTheme {
        AccessibilitySuggestionsSheet(null, {}, true, {}, persistentListOf(), {}, { _, _ -> })
    }
}
