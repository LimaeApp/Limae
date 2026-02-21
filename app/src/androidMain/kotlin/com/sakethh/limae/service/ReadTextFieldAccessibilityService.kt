package com.sakethh.limae.service

import android.accessibilityservice.AccessibilityService
import android.graphics.PixelFormat
import android.os.Bundle
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.sakethh.limae.AccessibilitySuggestionsSheet
import com.sakethh.limae.OverlayLifecycleOwner
import com.sakethh.limae.data.repository.SuggestionsRepoImpl
import com.sakethh.limae.domain.SuggestionEngine
import com.sakethh.limae.domain.model.LimaeSuggestionBundle
import com.sakethh.limae.domain.repository.SuggestionsRepo
import com.sakethh.limae.platform.HarperEngine
import com.sakethh.limae.platform.LanguageToolEngine
import com.sakethh.limae.ui.Icons
import com.sakethh.limae.ui.common.ItemState
import com.sakethh.limae.ui.theme.LimaeTheme
import com.sakethh.limae.utils.onFailure
import com.sakethh.limae.utils.onSuccess
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
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

    private val suggestionsRepo: SuggestionsRepo = SuggestionsRepoImpl(
        harperEngineRepo = HarperEngine,
        languageToolEngineRepo = LanguageToolEngine
    )
    private var focusedTextFieldText by mutableStateOf("")

    private val _suggestionsResult =
        MutableStateFlow<ItemState<PersistentList<LimaeSuggestionBundle>>>(
            ItemState(
                isError = false,
                errorMessage = null,
                isLoading = false,
                data = persistentListOf()
            )
        )

    private val suggestionsResult = _suggestionsResult.asStateFlow()

    private var currentFocusedNode: AccessibilityNodeInfo? = null

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate() {
        super.onCreate()

        overlayLifecycleOwner.lifecycleScope.launch {
            snapshotFlow {
                focusedTextFieldText
            }.debounce(250).collectLatest { inputText ->
                suggestionsRepo.getSuggestions(inputText)
                    .onSuccess(_suggestionsResult::onSuccess)
                    .onFailure(_suggestionsResult::onFailure)
                println("limae_data:${_suggestionsResult.value.data} for input: $inputText")
            }
        }

        overlayLifecycleOwner.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        composeView = ComposeView(applicationContext).apply {
            overlayLifecycleOwner.attachTo(this)
            setContent {
                val suggestions by suggestionsResult.collectAsStateWithLifecycle()
                LimaeTheme {
                    AnimatedVisibility(showUI) {
                        if (isExpanded) {
                            AccessibilitySuggestionsSheet(
                                onDismissRequest = {
                                    windowParams.gravity = Gravity.TOP or Gravity.START
                                    windowParams.x = lastX
                                    windowParams.y = lastY
                                    windowParams.width = WindowManager.LayoutParams.WRAP_CONTENT
                                    windowManager.updateViewLayout(composeView, windowParams)
                                    isExpanded = false
                                },
                                suggestions = suggestions.data,
                                onAddToDictionary = {},
                                onSuggestionAccept = { suggestionIndex, suggestion ->
                                    val (limaeSuggestion, suggestionEngine) = suggestions.data[suggestionIndex]

                                    val startIndex = limaeSuggestion.startIndex
                                    val endIndex = limaeSuggestion.endIndex

                                    val replacementText = limaeSuggestion
                                        .suggestions[suggestion].run {
                                        if (suggestionEngine == SuggestionEngine.Harper) {
                                            this.substringAfter(
                                                "“"
                                            ).substringBeforeLast("”")
                                        } else this
                                    }

                                    val replacedText =
                                        currentFocusedNode?.text?.replaceRange(
                                            startIndex = startIndex,
                                            endIndex = endIndex,
                                            replacement = replacementText
                                        )
                                    currentFocusedNode?.performAction(
                                        AccessibilityNodeInfo.ACTION_SET_TEXT,
                                        Bundle().apply {
                                            putCharSequence(
                                                AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                                                replacedText
                                            )
                                        }
                                    )
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

        if (showUI) {
            focusedTextFieldText = focusedNode?.text?.toString() ?: ""
        }
        currentFocusedNode = focusedNode
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