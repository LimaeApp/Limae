package com.sakethh.limae.domain

import kotlinx.coroutines.CoroutineDispatcher

interface LimaeDispatchers {
    val IO: CoroutineDispatcher
}