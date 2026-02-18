package com.sakethh.limae.platform

import android.content.res.Configuration
import com.sakethh.limae.HarperJVMEngine
import com.sakethh.limae.domain.LanguageToolEngine
import com.sakethh.limae.model.EngineSuggestion

actual object HarperEngine : com.sakethh.limae.model.HarperEngine {
    actual override suspend fun checkText(text: String): List<EngineSuggestion> {
        return HarperJVMEngine.checkText(text)
    }
}

actual val platform: Platform
    get() = run {
        val configuration = Configuration()
        configuration.setToDefaults()
        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) Platform.AndroidTablet else Platform.AndroidMobile
    }

actual object LanguageToolEngine : LanguageToolEngine {
    actual override suspend fun checkText(text: String): List<EngineSuggestion> = emptyList()
}