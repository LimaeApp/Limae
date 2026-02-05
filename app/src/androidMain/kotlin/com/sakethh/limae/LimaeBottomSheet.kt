package com.sakethh.limae

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import com.sakethh.limae.ui.Icons
import com.sakethh.limae.ui.theme.LimaeTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LimaeBottomSheet(onDismissRequest: () -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 300.dp)
            .background(MaterialTheme.colorScheme.surface)
    ) {
        stickyHeader {
            Surface {
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
                            fontSize = 22.sp
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
            }
        }
    }
}

@Composable
@Preview
private fun LimaeBottomSheetPreview(){
    LimaeTheme(darkTheme = true) {
        LimaeBottomSheet({})
    }
}