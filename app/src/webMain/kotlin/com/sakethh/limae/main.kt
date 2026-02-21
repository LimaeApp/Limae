package com.sakethh.limae

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.sakethh.limae.di.initializeKoin
import com.sakethh.limae.ui.Limae
import com.sakethh.limae.ui.theme.LimaeTheme

@OptIn(ExperimentalComposeUiApi::class)
suspend fun main() {
    initializeKoin()
    ComposeViewport {
        LimaeTheme(darkTheme = true) {
            Limae()
        }
    }
}