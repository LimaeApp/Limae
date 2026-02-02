package com.sakethh.limae.platform

import com.sakethh.limae.domain.model.LimaeError

expect object HarperEngine {
    fun checkText(text: String): List<LimaeError>
}