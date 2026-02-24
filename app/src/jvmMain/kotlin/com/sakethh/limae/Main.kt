package com.sakethh.limae

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.sakethh.limae.di.initializeKoin
import com.sakethh.limae.ui.Limae
import com.sakethh.limae.ui.common.KeyEventTunnel
import com.sakethh.limae.ui.theme.LimaeTheme
import com.sakethh.limae.utils.LimaePreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.awt.Dimension

suspend fun main() {
    initializeKoin()
    LimaePreferences.loadAll()
    val keyEventTunnelScope = CoroutineScope(Dispatchers.Default)
    application {
        Window(
            onKeyEvent = {
                keyEventTunnelScope.launch {
                    KeyEventTunnel.send(it)
                }
                true
            },
            onCloseRequest = ::exitApplication,
            title = "Limae",
        ) {
            this.window.minimumSize = Dimension(800, 600)
            LimaeTheme {
                Limae()
            }
        }
    }
}
