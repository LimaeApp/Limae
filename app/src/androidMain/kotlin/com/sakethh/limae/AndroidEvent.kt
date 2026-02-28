package com.sakethh.limae

import android.net.Uri
import com.sakethh.limae.utils.Constants
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

sealed interface AndroidEvent {
    data class PickAFile(
        val id: Int = Constants.IMPORT_ID,
    ) : AndroidEvent

    data class PickedFile(
        val uri: Uri?,
        val id: Int = Constants.IMPORT_ID,
    ) : AndroidEvent

    data class PickADirectory(
        val id: Int = Constants.PICK_DIR_ID,
    ) : AndroidEvent

    data class PickedDirectory(
        val uri: Uri?,
        val id: Int = Constants.PICK_DIR_ID,
    ) : AndroidEvent

    companion object {
        private val events = MutableSharedFlow<AndroidEvent>()

        val readEvents by lazy {
            events.asSharedFlow()
        }

        suspend fun pushEvent(event: AndroidEvent) {
            events.emit(event)
        }
    }
}
