package com.sakethh.limae.service

import android.accessibilityservice.AccessibilityService
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.sakethh.limae.OverlayLifecycleOwner
import com.sakethh.limae.R
import com.sakethh.limae.domain.SuggestionEngine
import com.sakethh.limae.domain.model.LimaeSuggestionBundle
import com.sakethh.limae.domain.onFailure
import com.sakethh.limae.domain.onSuccess
import com.sakethh.limae.domain.repository.AppBlocklistRepo
import com.sakethh.limae.domain.repository.SuggestionsRepo
import com.sakethh.limae.platform.Platform
import com.sakethh.limae.ui.common.AccessibilitySuggestionsSheet
import com.sakethh.limae.ui.common.ItemState
import com.sakethh.limae.ui.theme.LimaeTheme
import com.sakethh.limae.utils.LimaePreferences
import com.sakethh.limae.utils.onFailure
import com.sakethh.limae.utils.onSuccess
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.getKoin
import kotlin.math.roundToInt

class ReadTextFieldAccessibilityService : AccessibilityService() {
    companion object {
        private val _connected =
            MutableStateFlow(
                value = false,
            )
        val connected = _connected.asStateFlow()
    }

    private lateinit var windowManager: WindowManager
    private lateinit var composeView: ComposeView
    private val overlayLifecycleOwner = OverlayLifecycleOwner()

    private var lastX = 0
    private var lastY = 100
    private val windowParams: WindowManager.LayoutParams =
        WindowManager
            .LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT,
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                x = lastX
                y = lastY
            }

    private var isExpanded by mutableStateOf(false)
    private var showUI by mutableStateOf(false)

    private val suggestionsRepo: SuggestionsRepo = getKoin().get()

    private val appBlocklistRepo: AppBlocklistRepo = getKoin().get()

    var foregroundApp: Platform.Actions.InstalledApp? by mutableStateOf(null)

    private val foregroundChannel = Channel<String>()

    val isFocusedAppBlocked =
        appBlocklistRepo
            .getAllBlockedApps()
            .flatMapLatest { appBlockList ->
                snapshotFlow {
                    foregroundApp
                }.distinctUntilChanged().transform { foregroundApp ->
                    emit(
                        appBlockList.find { _foregroundApp ->
                            _foregroundApp.packageName == foregroundApp?.packageName
                        } != null,
                    )
                }
            }.stateIn(
                scope = this.overlayLifecycleOwner.lifecycleScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = true,
            )

    private suspend fun getForegroundApp(packageName: String): Platform.Actions.InstalledApp =
        withContext(Dispatchers.IO) {
            val applicationInfo =
                applicationContext.packageManager
                    .getInstalledApplications(
                        PackageManager.GET_META_DATA,
                    ).find {
                        it.packageName == packageName
                    }!!

            val packageLabel =
                packageManager
                    .getApplicationLabel(applicationInfo)
                    .toString()

            Platform.Actions.InstalledApp(
                name =
                packageLabel,
                packageName = packageName,
            )
        }

    private var focusedTextFieldValue by mutableStateOf("")

    private val suggestionsResult =
        MutableStateFlow<ItemState<PersistentList<LimaeSuggestionBundle>>>(
            ItemState(
                isError = false,
                errorMessage = null,
                isLoading = false,
                data = persistentListOf(),
            ),
        )

    private var currentFocusedNode: AccessibilityNodeInfo? = null
    private val blockingAppOpMutex = Mutex()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate() {
        super.onCreate()
        overlayLifecycleOwner.lifecycleScope.launch {
            launch {
                foregroundChannel.consumeAsFlow().collectLatest { packageName ->
                    foregroundApp = getForegroundApp(packageName)
                }
            }

            launch {
                snapshotFlow {
                    focusedTextFieldValue
                }.transform {
                    if (!isFocusedAppBlocked.value) {
                        emit(it)
                    }
                }.debounce(250)
                    .flatMapLatest { inputText ->
                        suggestionsRepo
                            .getSuggestions(inputText)
                    }.collectLatest { suggestionsResult ->
                        suggestionsResult
                            .onSuccess { (suggestions) ->
                                this@ReadTextFieldAccessibilityService.suggestionsResult.onSuccess(
                                    suggestions,
                                )
                            }.onFailure(this@ReadTextFieldAccessibilityService.suggestionsResult::onFailure)
                    }
            }
        }

        overlayLifecycleOwner.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        composeView =
            ComposeView(applicationContext).apply {
                overlayLifecycleOwner.attachTo(this)
                setContent {
                    val isFocusedAppBlocked by isFocusedAppBlocked.collectAsStateWithLifecycle()
                    if (isFocusedAppBlocked) return@setContent

                    val suggestions by suggestionsResult.collectAsStateWithLifecycle()
                    LimaeTheme {
                        AnimatedVisibility(showUI) {
                            if (isExpanded) {
                                AccessibilitySuggestionsSheet(
                                    foregroundApp = foregroundApp,
                                    onDismissRequest = {
                                        windowParams.gravity = Gravity.TOP or Gravity.START
                                        windowParams.x = lastX
                                        windowParams.y = lastY
                                        windowParams.width = WindowManager.LayoutParams.WRAP_CONTENT
                                        windowManager.updateViewLayout(composeView, windowParams)
                                        isExpanded = false
                                    },
                                    suggestions = suggestions.data,
                                    onAddToDictionary = {
                                        this@ReadTextFieldAccessibilityService.overlayLifecycleOwner.lifecycleScope.launch {
                                            suggestionsRepo.addStringsToDictionary(listOf(it.suggestion.errorSequence))
                                        }
                                    },
                                    onSuggestionAccept = { suggestionIndex, suggestion ->
                                        val (limaeSuggestion, suggestionEngine) = suggestions.data[suggestionIndex]

                                        val startIndex = limaeSuggestion.startIndex
                                        val endIndex = limaeSuggestion.endIndex

                                        val replacementText =
                                            limaeSuggestion.suggestions[suggestion].run {
                                                if (suggestionEngine == SuggestionEngine.Harper) {
                                                    this
                                                        .substringAfter(
                                                            "“",
                                                        ).substringBeforeLast("”")
                                                } else {
                                                    this
                                                }
                                            }

                                        val replacedText =
                                            currentFocusedNode?.text?.replaceRange(
                                                startIndex = startIndex,
                                                endIndex = endIndex,
                                                replacement = replacementText,
                                            )
                                        currentFocusedNode?.performAction(
                                            AccessibilityNodeInfo.ACTION_SET_TEXT,
                                            Bundle().apply {
                                                putCharSequence(
                                                    AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                                                    replacedText,
                                                )
                                            },
                                        )
                                    },
                                    onBlockRequest = {
                                        val packageName =
                                            foregroundApp?.packageName
                                                ?: return@AccessibilitySuggestionsSheet

                                        overlayLifecycleOwner.lifecycleScope.launch {
                                            blockingAppOpMutex.withLock {
                                                appBlocklistRepo.blockAnApp(packageName)
                                            }
                                        }
                                    },
                                )
                            } else {
                                Image(
                                    painter = painterResource(R.drawable.secretary_bird),
                                    contentDescription = "Expands the Limae Interface",
                                    modifier =
                                        Modifier
                                            .size(LimaePreferences.accessibilityIconSize.dp)
                                            .clip(CircleShape)
                                            .clickable {
                                                windowParams.gravity = Gravity.BOTTOM
                                                windowParams.x = 0
                                                windowParams.y = 0
                                                windowParams.width =
                                                    WindowManager.LayoutParams.MATCH_PARENT
                                                windowManager.updateViewLayout(
                                                    composeView,
                                                    windowParams,
                                                )
                                                isExpanded = true
                                            }.pointerInput(Unit) {
                                                detectDragGestures { change, dragAmount ->
                                                    change.consume()

                                                    windowParams.x += dragAmount.x.roundToInt()
                                                    windowParams.y += dragAmount.y.roundToInt()

                                                    windowManager.updateViewLayout(
                                                        composeView,
                                                        windowParams,
                                                    )

                                                    lastX = windowParams.x
                                                    lastY = windowParams.y
                                                }
                                            },
                                )
                            }
                        }
                    }
                }
                overlayLifecycleOwner.onResume()
            }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return

        val rootInActiveWindow =
            try {
                this@ReadTextFieldAccessibilityService.rootInActiveWindow
            } catch (_: Exception) {
                null
            }

        if (rootInActiveWindow == null) {
            showUI = false
            return
        }

        val focusedNode =
            try {
                rootInActiveWindow.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
            } catch (_: Exception) {
                null
            }

        showUI = focusedNode != null && focusedNode.isEditable && !focusedNode.isPassword

        if (showUI) {
            focusedTextFieldValue = focusedNode?.text?.toString() ?: ""
            foregroundChannel.trySend(rootInActiveWindow.packageName.toString())
        }
        currentFocusedNode = focusedNode
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        showOverlay()
        _connected.tryEmit(true)
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
        _connected.tryEmit(false)
    }
}
