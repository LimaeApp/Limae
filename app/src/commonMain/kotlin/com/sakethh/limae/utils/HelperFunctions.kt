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
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

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


@OptIn(ExperimentalTime::class)
fun epochToReadableDateTime(
    epochSeconds: Long,
    timeZone: TimeZone = TimeZone.currentSystemDefault()
): String? {
    return try {
        Instant.fromEpochSeconds(epochSeconds).toLocalDateTime(timeZone)
            .run {
                "${"${this.date.day}".addZeroAtPrefixOnInt()} ${
                    month.name.initialCaps()
                } ${this.year}, ${
                    "${
                        (if (this.time.hour > 12) time.hour - 12 else time.hour)
                    }".addZeroAtPrefixOnInt()
                }:${"${this.time.minute}".addZeroAtPrefixOnInt()}:${"${this.time.second}".addZeroAtPrefixOnInt()} ${if (this.time.hour > 11) "PM" else "AM"}"
            }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun String.addZeroAtPrefixOnInt() =
    try {
        if (toInt() > 9) this else "0$this"
    } catch (e: Exception) {
        e.printStackTrace()
        this
    }


fun String.initialCaps(): String {
    return when {
        length > 1 -> {
            get(0).uppercase() + substring(1).lowercase()
        }

        else -> this.uppercase()
    }
}