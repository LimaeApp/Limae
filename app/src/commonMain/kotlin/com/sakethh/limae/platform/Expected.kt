package com.sakethh.limae.platform

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import com.sakethh.limae.domain.EngineSuggestion
import com.sakethh.limae.domain.HarperEngineRepo
import com.sakethh.limae.domain.LanguageToolEngineRepo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
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
