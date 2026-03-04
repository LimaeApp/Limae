package com.sakethh.limae.platform

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import com.sakethh.limae.domain.EngineSuggestion
import com.sakethh.limae.domain.HarperEngineRepo
import com.sakethh.limae.domain.LanguageToolEngineRepo
import com.sakethh.limae.utils.NonWebRunBlocking
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.module.Module

expect object HarperEngine : HarperEngineRepo {
    override suspend fun checkText(text: String): List<EngineSuggestion>
}

expect object LanguageToolEngine : LanguageToolEngineRepo {
    override suspend fun checkText(text: String): List<EngineSuggestion>
}

expect val platform: Platform

expect val LimaeIODispatcher: CoroutineDispatcher

expect suspend fun platformDatabaseModule(): Module

@Composable
expect fun dynamicLightTheme(): ColorScheme

@Composable
expect fun dynamicDarkTheme(): ColorScheme

expect val isReadTextFieldAccessibilityServiceRunning: StateFlow<Boolean>

@NonWebRunBlocking
/**
`runBlockingNonWeb` works as expected on Android and Desktop, but the
native implementation is unavailable on Web via Wasm, so this fallback
essentially does absolutely nothing there. AVOID USING THIS UNLESS IT
DOES NOT IMPACT WEB USAGE, because a proper solution matching standard
behavior won't likely arrive anytime soon.
 * */
expect inline fun runBlockingNonWeb(
    crossinline block: suspend () -> Unit,
)
