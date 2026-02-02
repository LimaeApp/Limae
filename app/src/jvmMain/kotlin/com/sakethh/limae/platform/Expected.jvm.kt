package com.sakethh.limae.platform

import com.sakethh.limae.HarperEngine
import com.sakethh.limae.domain.model.LimaeError

actual object HarperEngine {
    actual fun checkText(text: String): List<LimaeError> {
        return HarperEngine.checkText(text) as (List<LimaeError>)
    }
}