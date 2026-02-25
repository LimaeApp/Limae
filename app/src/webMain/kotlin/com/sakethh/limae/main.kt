package com.sakethh.limae

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.sakethh.limae.di.initializeKoin
import com.sakethh.limae.platform.Platform
import com.sakethh.limae.ui.Limae
import com.sakethh.limae.ui.theme.LimaeTheme
import org.koin.dsl.bind
import org.koin.dsl.module

@OptIn(ExperimentalComposeUiApi::class)
suspend fun main() {
    initializeKoin {
        modules(
            module {
                single {
                    object : Platform.Actions {
                        override fun openAccessibilitySettings() = Unit
                    }
                }.bind<Platform.Actions>()
            },
        )
    }
    ComposeViewport {
        LimaeTheme {
            Limae()
        }
    }
}
