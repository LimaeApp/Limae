package com.sakethh.limae.domain

sealed interface Result<T> {
    data class Success<T>(val data: T) : Result<T>
    data class Failure<T>(val throwable: Throwable) : Result<T>
}

suspend fun <T> Result<T>.onSuccess(init: suspend (Result.Success<T>) -> Unit): Result<T> {
    if (this is Result.Success) {
        init(this)
    }
    return this
}

suspend fun <T> Result<T>.onFailure(init: suspend (Throwable) -> Unit): Result<T> {
    if (this is Result.Failure) {
        init(this.throwable)
    }
    return this
}