package com.sakethh.limae

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.sakethh.limae.ui.Limae
import com.sakethh.limae.ui.theme.LimaeTheme

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Limae",
    ) {
        LimaeTheme(darkTheme = true) {
            Limae()
        }
    }
}