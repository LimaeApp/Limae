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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sakethh.limae.model.LimaeNote
import com.sakethh.limae.ui.Icons

@Composable
fun SuggestionNote(limaeNote: LimaeNote) {
    Card(
        modifier = Modifier.padding(
            end = 15.dp,
            start = 15.dp,
            top = 2.5.dp,
            bottom = 2.5.dp
        )
            .fillMaxWidth()
    ) {
        Column {
            Text(
                text = limaeNote.kind.name,
                style = MaterialTheme.typography.titleMedium,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.clip(
                    RoundedCornerShape(
                        bottomStart = 25.dp,
                        bottomEnd = 25.dp
                    )
                ).background(MaterialTheme.colorScheme.primaryContainer)
                    .fillMaxWidth().padding(15.dp)
            )
            Spacer(modifier = Modifier.height(7.5.dp))
            Text(
                text = limaeNote.message,
                style = MaterialTheme.typography.titleMedium,
                fontSize = 16.sp,
                modifier = Modifier.padding(
                    start = 15.dp,
                    end = 15.dp
                )
            )
            Spacer(modifier = Modifier.height(5.dp))
            FlowRow(
                modifier = Modifier.padding(
                    start = 15.dp,
                    end = 15.dp
                ),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                limaeNote.suggestions.forEach {
                    key(it) {
                        Button(modifier = Modifier.showHandOnHover(), onClick = {

                        }) {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(7.5.dp))
            Row(
                modifier = Modifier.clip(
                    RoundedCornerShape(
                        topStart = 25.dp,
                        topEnd = 25.dp
                    )
                ).showHandOnHover().clickable(onClick = {

                }).background(MaterialTheme.colorScheme.secondaryContainer)
                    .fillMaxWidth().padding(15.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AddNotes,
                    contentDescription = "Add this suggestion to dictionary to not include in any suggestions."
                )
                Spacer(Modifier.width(5.dp))
                Text(
                    text = "Add to dictionary",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }

}