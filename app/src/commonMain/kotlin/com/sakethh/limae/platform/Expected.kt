package com.sakethh.limae.platform

import com.sakethh.limae.model.HarperEngine
import com.sakethh.limae.model.LimaeSuggestionNote

expect object HarperEngine: HarperEngine {
    override fun checkText(text: String): List<LimaeSuggestionNote>
}

expect val platform: Platform