package com.sakethh.limae.ui

import com.sakethh.limae.ui.navigation.NavRoute

sealed interface LimaeAction {
    data object NavigateBack : LimaeAction
    data class Navigate(val destination: NavRoute) : LimaeAction
}