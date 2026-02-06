package com.sakethh.limae.platform

import com.sakethh.limae.HarperEngine
import com.sakethh.limae.model.LimaeSuggestionNote

actual object HarperEngine: com.sakethh.limae.model.HarperEngine {
    actual override fun checkText(text: String): List<LimaeSuggestionNote> {
        return HarperEngine.checkText(text)
    }
}

actual val platform: Platform = Platform.Desktop