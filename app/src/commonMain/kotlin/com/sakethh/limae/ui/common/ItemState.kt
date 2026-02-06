package com.sakethh.limae.ui.common

import androidx.compose.runtime.Stable

@Stable
data class ItemState<T>(
    val isError: Boolean,
    val errorMessage: String?,
    val isLoading: Boolean,
    val data: T
)