package com.sakethh.limae.service

import android.accessibilityservice.AccessibilityService
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import com.sakethh.limae.LimaeBottomSheet
import com.sakethh.limae.OverlayLifecycleOwner
import com.sakethh.limae.ui.Icons
import com.sakethh.limae.ui.theme.LimaeTheme
import kotlin.math.roundToInt

class ReadTextFieldAccessibilityService : AccessibilityService() {
    private lateinit var windowManager: WindowManager
    private lateinit var composeView: ComposeView
    private val overlayLifecycleOwner = OverlayLifecycleOwner()

    private var lastX = 0
    private var lastY = 100
    private val windowParams: WindowManager.LayoutParams = WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        PixelFormat.TRANSLUCENT
    ).apply {
        gravity = Gravity.TOP or Gravity.START
        x = lastX
        y = lastY
    }


    private var isExpanded by mutableStateOf(false)
    private var showUI by mutableStateOf(false)

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate() {
        super.onCreate()
        overlayLifecycleOwner.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        composeView = ComposeView(applicationContext).apply {
            overlayLifecycleOwner.attachTo(this)
            setContent {
                LimaeTheme {
                    AnimatedVisibility(showUI) {
                        if (isExpanded) {
                            LimaeBottomSheet(
                                onDismissRequest = {
                                    windowParams.gravity = Gravity.TOP or Gravity.START
                                    windowParams.x = lastX
                                    windowParams.y = lastY
                                    windowParams.width = WindowManager.LayoutParams.WRAP_CONTENT
                                    windowManager.updateViewLayout(composeView, windowParams)
                                    isExpanded = false
                                },
                            )
                        } else {
                            FilledTonalIconButton(onClick = {
                                windowParams.gravity = Gravity.BOTTOM
                                windowParams.x = 0
                                windowParams.y = 0
                                windowParams.width = WindowManager.LayoutParams.MATCH_PARENT
                                windowManager.updateViewLayout(composeView, windowParams)
                                isExpanded = true
                            }, modifier = Modifier.pointerInput(Unit) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()

                                    windowParams.x += dragAmount.x.roundToInt()
                                    windowParams.y += dragAmount.y.roundToInt()

                                    windowManager.updateViewLayout(composeView, windowParams)

                                    lastX = windowParams.x
                                    lastY = windowParams.y
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.EditNote,
                                    contentDescription = "Expands the Limae Interface"
                                )
                            }
                        }
                    }
                }
            }
            overlayLifecycleOwner.onResume()
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return

        val root = try {
            rootInActiveWindow
        } catch (_: Exception) {
            null
        }

        if (root == null) {
            showUI = false
            return
        }

        val focusedNode = try {
            root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
        } catch (_: Exception) {
            null
        }

        showUI = focusedNode != null && focusedNode.isEditable && !focusedNode.isPassword
    }



    override fun onServiceConnected() {
        super.onServiceConnected()
        showOverlay()
        println("ReadTextFieldAccessibilityService Connected")
    }

    private fun showOverlay() {
        if (!::composeView.isInitialized) return
        if (!composeView.isAttachedToWindow) {
            windowManager.addView(composeView, windowParams)
        }
    }

    private fun hideOverlay() {
        if (::composeView.isInitialized && composeView.isAttachedToWindow) {
            windowManager.removeView(composeView)
        }
    }

    override fun onInterrupt() {
        println("ReadTextFieldAccessibilityService Interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        overlayLifecycleOwner.onDestroy()
        hideOverlay()
    }
}