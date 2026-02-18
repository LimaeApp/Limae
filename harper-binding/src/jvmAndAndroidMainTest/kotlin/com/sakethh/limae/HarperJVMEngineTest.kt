package com.sakethh.limae

import kotlin.test.Test
import kotlin.test.assertTrue

class HarperJVMEngineTest {

    @Test
    fun testBindingWithError() {
        val text = "Since your data never leaves your device, you don't ned too worry aout us selling it or using it to train large language models."

        val errors = HarperJVMEngine.checkText(text)

        println("Errors found: $errors")

        assertTrue(errors.isNotEmpty(), "HarperEngineTest should have detected the errors.")
    }

    @Test
    fun testBindingWithCleanText() {
        val text = "Harper can be a lifesaver when writing technical documents, emails, or other formal forms of communication."
        val errors = HarperJVMEngine.checkText(text)

        assertTrue(errors.isEmpty(), "HarperEngineTest should return empty list for valid text")
    }
}