package com.sakethh.limae.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.sakethh.limae.ui.navigation.NavRoute
import com.sakethh.limae.ui.screens.home.HomeScreen
import com.sakethh.limae.ui.screens.note.NoteScreen

@Composable
fun Limae() {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        val navController = rememberNavController()
        val takeAction: (LimaeAction) -> Unit = retain {
            { action ->
                when (action) {
                    is LimaeAction.Navigate -> navController.navigate(action.destination) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }

                    LimaeAction.NavigateBack -> navController.navigateUp()
                }
            }
        }
        NavHost(navController = navController, startDestination = NavRoute.Home) {
            composable<NavRoute.Home> {
                HomeScreen(takeAction = {
                    takeAction(it)
                })
            }
            composable<NavRoute.Note> { navBackStackEntry ->
                val note = navBackStackEntry.toRoute<NavRoute.Note>()
                NoteScreen(
                    takeAction = {
                        takeAction(it)
                    },
                    noteId = note.noteId,
                )
            }
        }
    }
}