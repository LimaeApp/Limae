package com.sakethh.limae.ui.common

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon

// i should have a separate lib for these, since there are others too from linkora

fun Modifier.showHandOnHover() = this.pointerHoverIcon(PointerIcon.Hand)