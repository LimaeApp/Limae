package com.sakethh.limae.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.sakethh.limae.platform.Platform
import com.sakethh.limae.platform.platform
import com.sakethh.limae.ui.navigation.NavRoute
import com.sakethh.limae.ui.screens.home.HomeScreen
import com.sakethh.limae.ui.screens.note.NoteScreen
import com.sakethh.limae.ui.screens.settings.SettingsScreen
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun Limae() {
    val limaeVM = koinViewModel<LimaeVM>()
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    var fabHeight by remember {
        mutableStateOf(0.dp)
    }
    val snackbarBtmPadding by animateDpAsState(
        if (navBackStackEntry?.destination?.hasRoute<NavRoute.Home>() ==
            true
        ) {
            fabHeight * 1.25f
        } else {
            0.dp
        },
    )
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Box(
            modifier = Modifier.zIndex(100f).fillMaxSize(),
            contentAlignment = Alignment.BottomCenter,
        ) {
            SnackbarHost(
                hostState = limaeVM.snackBarHost,
                modifier =
                    Modifier
                        .padding(
                            bottom = snackbarBtmPadding,
                            start = 7.5.dp,
                            end = 7.5.dp,
                        ).fillMaxWidth()
                        .navigationBarsPadding(),
            ) { snackbarData ->
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(7.5.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .background(MaterialTheme.colorScheme.onPrimary)
                            .border(
                                width = 1.5.dp,
                                color = MaterialTheme.colorScheme.primary.copy(0.25f),
                                shape = RoundedCornerShape(15.dp),
                            ).padding(15.dp),
                ) {
                    Text(
                        text = snackbarData.visuals.message,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 18.sp,
                        maxLines = 3,
                    )
                }
            }
        }
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

                        else -> {
                            Unit
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
                HomeScreen(FABHeight = {
                    fabHeight = it
                }, takeAction = {
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
