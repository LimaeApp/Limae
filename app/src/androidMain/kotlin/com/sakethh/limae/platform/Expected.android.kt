package com.sakethh.limae.platform

import android.content.res.Configuration
import com.sakethh.limae.HarperEngine
import com.sakethh.limae.model.LimaeSuggestionNote

actual object HarperEngine: com.sakethh.limae.model.HarperEngine {
    actual override fun checkText(text: String): List<LimaeSuggestionNote> {
        return HarperEngine.checkText(text)
    }
}

actual val platform: Platform
    get() = run {
        val configuration = Configuration()
        configuration.setToDefaults()
        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) Platform.AndroidTablet else Platform.AndroidMobile
    }