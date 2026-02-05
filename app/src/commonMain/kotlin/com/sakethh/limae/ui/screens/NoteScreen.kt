package com.sakethh.limae.ui.screens

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sakethh.limae.ui.Icons
import com.sakethh.limae.ui.LimaeAction
import com.sakethh.limae.ui.common.showHandOnHover

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteScreen(
    takeAction: (LimaeAction) -> Unit,
    title: String,
    content: String,
    lastSavedOn: String
) {
    val topAppBarScrollBehaviour = TopAppBarDefaults.pinnedScrollBehavior()
    var title by rememberSaveable(title) {
        mutableStateOf(title)
    }
    var content by rememberSaveable(content) {
        mutableStateOf(content)
    }
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
        TopAppBar(scrollBehavior = topAppBarScrollBehaviour, title = {
            Text(text = "Limae")
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
    }) { paddingValues ->
        LazyColumn(
            contentPadding = paddingValues,
            modifier = Modifier.nestedScroll(topAppBarScrollBehaviour.nestedScrollConnection)
                .fillMaxSize()
        ) {
            item {
                TextField(
                    placeholder = {
                        Text(
                            text = "Title",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    },
                    value = title,
                    onValueChange = {
                        title = it
                    },
                    textStyle = TextStyle(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                    ),
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
                            fontWeight = FontWeight.Normal,
                        )
                    },
                    value = content,
                    onValueChange = {
                        content = it
                    },
                    textStyle = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal,
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
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}