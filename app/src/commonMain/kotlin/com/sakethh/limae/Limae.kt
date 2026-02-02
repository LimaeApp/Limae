package com.sakethh.limae

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.sakethh.limae.platform.HarperEngine

@Composable
fun Limae(){
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        LaunchedEffect(Unit) {
            HarperEngine.checkText("There are some cases where the the standard grammar checkers don't cut it. That;s where Harper comes in handy.").run {
                println("Here:\n$this")
            }
        }
    }
}