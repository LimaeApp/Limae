package com.sakethh.limae.ui

import com.sakethh.limae.ui.navigation.NavRoute
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

sealed interface LimaeAction {
    data object NavigateBack : LimaeAction

    data class Navigate(
        val destination: NavRoute,
    ) : LimaeAction

    data class ShowSnackbar(
        val message: String,
    ) : LimaeAction

    companion object {
        private val events = MutableSharedFlow<LimaeAction>()
        val readEvents = events.asSharedFlow()

        suspend fun sendAction(limaeAction: LimaeAction) = events.emit(limaeAction)

        suspend fun reportError(throwable: Throwable) {
            throwable.printStackTrace()
            events.emit(ShowSnackbar(throwable.message ?: throwable.stackTraceToString()))
        }

        suspend fun reportMessage(message: String) {
            events.emit(ShowSnackbar(message))
        }
    }
}
