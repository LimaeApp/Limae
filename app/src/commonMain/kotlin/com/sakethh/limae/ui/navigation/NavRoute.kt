package com.sakethh.limae.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface NavRoute {
    @Serializable
    data object Main: NavRoute

    @Serializable
    data object Note: NavRoute
}