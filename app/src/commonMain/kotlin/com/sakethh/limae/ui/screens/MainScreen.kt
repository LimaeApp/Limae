package com.sakethh.limae.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.sakethh.limae.ui.Icons
import com.sakethh.limae.ui.LimaeAction
import com.sakethh.limae.ui.common.showHandOnHover
import com.sakethh.limae.ui.navigation.NavRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(takeAction: (LimaeAction) -> Unit) {
    val topAppBarScrollBehaviour = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(topBar = {
        LargeTopAppBar(title = {
            Text(text = "Limae")
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
            IconButton(modifier = Modifier.showHandOnHover(),onClick = {}) {
                Icon(
                    imageVector = Icons.Settings,
                    contentDescription = "Settings Icon button to navigate to the settings screen"
                )
            }
            FilledIconButton(modifier = Modifier.showHandOnHover(),onClick = {
                takeAction(LimaeAction.Navigate(destination = NavRoute.Note))
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

        }
    }
}