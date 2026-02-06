package com.sakethh.limae.platform

import com.sakethh.limae.model.LimaeNote

expect object HarperEngine {
    fun checkText(text: String): List<LimaeNote>
}