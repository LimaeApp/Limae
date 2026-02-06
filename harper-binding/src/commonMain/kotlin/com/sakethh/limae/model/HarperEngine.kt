package com.sakethh.limae.model

interface HarperEngine {
    fun checkText(text: String): List<LimaeSuggestionNote>
}