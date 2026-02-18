package com.sakethh.limae.utils

import com.sakethh.limae.domain.SuggestionEngine
import com.sakethh.limae.domain.model.LimaeSuggestion
import com.sakethh.limae.domain.model.LimaeSuggestionBundle
import com.sakethh.limae.ui.common.ItemState
import kotlinx.collections.immutable.PersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

fun <T> MutableStateFlow<ItemState<T>>.onLoading() {
    update {
        it.copy(isLoading = true, isError = false, errorMessage = null)
    }
}

fun <T> MutableStateFlow<ItemState<T>>.onFailure(throwable: Throwable) {
    update {
        it.copy(
            isLoading = false,
            isError = true,
            errorMessage = throwable.message ?: "Something went wrong."
        )
    }
}

//  MutableStateFlow

fun <T: PersistentList<LimaeSuggestionBundle>>  MutableStateFlow<ItemState<T>>.onSuccess(data: T) {
    update {
        it.copy(isLoading = false, isError = false, errorMessage = null, data = data)
    }
}