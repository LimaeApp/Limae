package com.sakethh.limae.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface NavRoute {
    @Serializable
    data object Onboarding : NavRoute

    @Serializable
    data object Home : NavRoute

    @Serializable
    data object Settings : NavRoute

    @Serializable
    data class Note(
        val noteId: String?,
        val showAccessibilityOverlay: Boolean,
    ) : NavRoute
}
