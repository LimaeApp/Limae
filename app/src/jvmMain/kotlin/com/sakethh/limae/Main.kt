package com.sakethh.limae

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.sakethh.limae.di.initializeKoin
import com.sakethh.limae.ui.Limae
import com.sakethh.limae.ui.theme.LimaeTheme
import java.awt.Dimension

suspend fun main() {
    initializeKoin()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Limae",
        ) {
            this.window.minimumSize = Dimension(800, 600)
            LimaeTheme(darkTheme = true) {
                Limae()
            }
        }
    }
}