package com.sakethh.limae.ui.common

import androidx.compose.ui.input.key.KeyEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object KeyEventTunnel {
    private val sendTunnel = MutableSharedFlow<KeyEvent>()

    val readTunnel = sendTunnel.asSharedFlow()

    suspend fun send(keyEvent: KeyEvent) {
        sendTunnel.emit(keyEvent)
    }
}
