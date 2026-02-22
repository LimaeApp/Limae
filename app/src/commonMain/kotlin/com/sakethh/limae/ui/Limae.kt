package com.sakethh.limae.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import com.sakethh.limae.platform.Platform
import com.sakethh.limae.platform.platform
import com.sakethh.limae.ui.navigation.NavRoute
import com.sakethh.limae.ui.screens.home.HomeScreen
import com.sakethh.limae.ui.screens.note.NoteScreen
import com.sakethh.limae.ui.screens.settings.SettingsScreen

@Composable
fun Limae() {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        val navController = rememberNavController()
        val takeAction: (LimaeAction) -> Unit =
            retain {
                { action ->
                    when (action) {
                        is LimaeAction.Navigate -> {
                            navController.navigate(action.destination) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }

                        LimaeAction.NavigateBack -> {
                            navController.navigateUp()
                        }
                    }
                }
            }
        NavHost(
            navController = navController,
            startDestination = if (platform.type == Platform.Type.Web) NavRoute.Note(noteId = null) else NavRoute.Home,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
        ) {
            composable<NavRoute.Home>(
                enterTransition = {
                    fadeIn(
                        animationSpec =
                            tween(
                                300,
                                easing = LinearEasing,
                            ),
                    ) +
                        slideIntoContainer(
                            animationSpec = tween(300, easing = EaseIn),
                            towards = AnimatedContentTransitionScope.SlideDirection.End,
                        )
                },
                exitTransition = {
                    fadeOut(
                        animationSpec =
                            tween(
                                300,
                                easing = LinearEasing,
                            ),
                    ) +
                        slideOutOfContainer(
                            animationSpec = tween(300, easing = EaseOut),
                            towards = AnimatedContentTransitionScope.SlideDirection.Start,
                        )
                },
            ) {
                HomeScreen(takeAction = {
                    takeAction(it)
                })
            }
            composable<NavRoute.Note>(
                enterTransition = {
                    fadeIn(
                        animationSpec =
                            tween(
                                300,
                                easing = LinearEasing,
                            ),
                    ) +
                        slideInVertically(
                            animationSpec =
                                tween(
                                    300,
                                    easing = LinearEasing,
                                ),
                        )
                },
                exitTransition = {
                    fadeOut(
                        animationSpec =
                            tween(
                                300,
                                easing = LinearEasing,
                            ),
                    ) +
                        slideOutVertically(
                            animationSpec =
                                tween(
                                    300,
                                    easing = LinearEasing,
                                ),
                        )
                },
            ) { navBackStackEntry ->
                val note = navBackStackEntry.toRoute<NavRoute.Note>()
                NoteScreen(
                    takeAction = {
                        takeAction(it)
                    },
                    noteId = note.noteId,
                )
            }
            composable<NavRoute.Settings>(
                enterTransition = {
                    fadeIn(
                        animationSpec =
                            tween(
                                300,
                                easing = LinearEasing,
                            ),
                    ) +
                        slideIntoContainer(
                            animationSpec = tween(300, easing = EaseIn),
                            towards = AnimatedContentTransitionScope.SlideDirection.Start,
                        )
                },
                exitTransition = {
                    fadeOut(
                        animationSpec =
                            tween(
                                300,
                                easing = LinearEasing,
                            ),
                    ) +
                        slideOutOfContainer(
                            animationSpec = tween(300, easing = EaseOut),
                            towards = AnimatedContentTransitionScope.SlideDirection.End,
                        )
                },
            ) {
                SettingsScreen(performAction = {
                    takeAction(it)
                })
            }
        }
    }
}
