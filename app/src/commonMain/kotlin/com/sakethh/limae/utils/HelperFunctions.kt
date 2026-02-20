package com.sakethh.limae.utils

import com.sakethh.limae.domain.model.LimaeSuggestionBundle
import com.sakethh.limae.ui.common.ItemState
import kotlinx.collections.immutable.PersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import com.sakethh.limae.domain.Result

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

fun getEpochSecond() = Clock.System.now().epochSeconds

@ExperimentalUuidApi
fun getRandomUUIDv7() = Uuid.generateV7().toString()

fun <T : PersistentList<LimaeSuggestionBundle>> MutableStateFlow<ItemState<T>>.onSuccess(data: T) {
    update {
        it.copy(isLoading = false, isError = false, errorMessage = null, data = data)
    }
}

inline fun <T> runSafe(block: () -> T): Result<T> {
    return try {
        Result.Success<T>(block())
    } catch (e: Throwable) {
        e.printStackTrace()
        Result.Failure(e)
    }
}