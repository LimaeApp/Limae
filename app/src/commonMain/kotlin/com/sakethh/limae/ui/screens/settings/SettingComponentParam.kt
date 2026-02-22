package com.sakethh.limae.ui.screens.settings

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.UriHandler

@Stable
data class SettingComponentParam(
    val title: String,
    val doesDescriptionExists: Boolean,
    val description: String?,
    val isSwitchNeeded: Boolean,
    val isSwitchEnabled: Boolean,
    val onSwitchStateChange: (newValue: Boolean) -> Unit,
    val onAcknowledgmentClick: (uriHandler: UriHandler) -> Unit = { },
    val icon: ImageVector? = null,
    val showIcon: Boolean,
    val showFilledIcon: Boolean = false,
    val showArrowIcon: Boolean = false
)
