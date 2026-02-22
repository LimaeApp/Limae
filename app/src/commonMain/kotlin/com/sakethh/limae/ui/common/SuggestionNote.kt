package com.sakethh.limae.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sakethh.limae.domain.LintKind
import com.sakethh.limae.domain.model.LimaeSuggestionBundle
import com.sakethh.limae.ui.Icons

@Composable
fun SuggestionNote(
    limaeSuggestionBundle: LimaeSuggestionBundle,
    onAddToDictionary: () -> Unit,
    onSuggestionAccept: (Int) -> Unit,
) {
    Card(
        modifier =
            Modifier
                .padding(
                    end = 15.dp,
                    start = 15.dp,
                    top = 2.5.dp,
                    bottom = 2.5.dp,
                ).fillMaxWidth(),
    ) {
        Column {
            Column(
                modifier =
                    Modifier
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .fillMaxWidth()
                        .padding(15.dp),
            ) {
                Text(
                    text = limaeSuggestionBundle.suggestion.kind.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    text = "via " + limaeSuggestionBundle.engine.name,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(0.75f),
                    style = MaterialTheme.typography.titleSmall,
                    fontSize = 14.sp,
                )
            }
            Spacer(modifier = Modifier.height(7.5.dp))
            Text(
                text = limaeSuggestionBundle.suggestion.message,
                style = MaterialTheme.typography.titleMedium,
                fontSize = 16.sp,
                modifier =
                    Modifier.padding(
                        start = 15.dp,
                        end = 15.dp,
                    ),
            )
            Spacer(modifier = Modifier.height(5.dp))
            FlowRow(
                modifier =
                    Modifier.padding(
                        start = 15.dp,
                        end = 15.dp,
                    ),
                verticalArrangement = Arrangement.spacedBy(5.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                limaeSuggestionBundle.suggestion.suggestions.forEachIndexed { index, suggestion ->
                    key(suggestion + index) {
                        Button(modifier = Modifier.showHandOnHover(), onClick = {
                            onSuggestionAccept(index)
                        }) {
                            Text(
                                text = suggestion,
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(7.5.dp))
            if (limaeSuggestionBundle.suggestion.kind == LintKind.Spelling) {
                Row(
                    modifier =
                        Modifier
                            .showHandOnHover()
                            .clickable(onClick = onAddToDictionary)
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .fillMaxWidth()
                            .padding(15.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.ListAltAdd,
                        contentDescription = "Add this suggestion to dictionary to not include in any suggestions.",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = "Add \"${limaeSuggestionBundle.suggestion.errorSequence}\" to dictionary",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            }
        }
    }
}
