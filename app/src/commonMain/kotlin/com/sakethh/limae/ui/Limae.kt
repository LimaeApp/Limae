package com.sakethh.limae.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sakethh.limae.ui.navigation.NavRoute
import com.sakethh.limae.ui.screens.MainScreen
import com.sakethh.limae.ui.screens.note.NoteScreen

@Composable
fun Limae() {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        val navController = rememberNavController()
        val takeAction: (LimaeAction) -> Unit = retain {
            { action ->
                when (action) {
                    is LimaeAction.Navigate -> navController.navigate(action.destination)
                    LimaeAction.NavigateBack -> navController.navigateUp()
                }
            }
        }
        NavHost(navController = navController, startDestination = NavRoute.Note) {
            composable<NavRoute.Main> {
                MainScreen(takeAction = {
                    takeAction(it)
                })
            }
            composable<NavRoute.Note> {
                NoteScreen(
                    takeAction = {
                        takeAction(it)
                    },
                    title = "",
                    content = "",
                    lastSavedOn = "18-02-1515"
                )
            }
        }
    }
}