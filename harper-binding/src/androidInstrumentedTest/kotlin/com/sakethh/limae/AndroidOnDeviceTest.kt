package com.sakethh.limae

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AndroidOnDeviceTest {

    @Test
    fun testHarperEngine() {
        HarperEngineTest().apply {
            testBindingWithCleanText()
            testBindingWithGrammarError()
        }
    }

}