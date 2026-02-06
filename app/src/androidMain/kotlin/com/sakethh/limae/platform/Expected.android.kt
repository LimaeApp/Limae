package com.sakethh.limae.platform

import com.sakethh.limae.HarperEngine
import com.sakethh.limae.model.LimaeNote

actual object HarperEngine {
    actual fun checkText(text: String): List<LimaeNote> {
        return HarperEngine.checkText(text)
    }
}