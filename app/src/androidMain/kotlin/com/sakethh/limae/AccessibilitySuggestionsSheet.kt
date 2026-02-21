package com.sakethh.limae

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sakethh.limae.domain.model.LimaeSuggestionBundle
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
    onDismissRequest: () -> Unit,
    suggestions: PersistentList<LimaeSuggestionBundle>,
    onAddToDictionary: (LimaeSuggestionBundle) -> Unit,
    onSuggestionAccept: (LimaeNotesIndex, SuggestionNoteIndex) -> Unit
) {
    Surface {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 150.dp, max = 500.dp)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                HorizontalDivider(thickness = 2.5.dp)
                Row(
                    modifier = Modifier
                        .padding(10.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Limae",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 22.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onDismissRequest) {
                        Icon(
                            imageVector = Icons.Close,
                            contentDescription = "Minimize Limae's correction notes bottom sheet."
                        )
                    }
                }
                HorizontalDivider(thickness = 2.5.dp)
            }
            SuggestionsList(
                suggestions = suggestions,
                modifier = Modifier.fillMaxWidth(),
                showStickyHeader = false,
                onAddToDictionary = onAddToDictionary,
                onSuggestionAccept = onSuggestionAccept,
            )
        }
    }
}

@Composable
@Preview
private fun AccessibilitySuggestionsSheetPreview() {
    LimaeTheme(darkTheme = true) {
        AccessibilitySuggestionsSheet({}, persistentListOf(), {}, { _, _ -> })
    }
}