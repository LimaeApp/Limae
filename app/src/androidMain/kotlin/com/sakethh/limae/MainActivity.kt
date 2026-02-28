package com.sakethh.limae

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModel
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.sakethh.limae.ui.Limae
import com.sakethh.limae.ui.theme.LimaeTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            LimaeTheme {
                Limae()
                val importFilePicker =
                    rememberLauncherForActivityResult(
                        ActivityResultContracts.GetContent(),
                    ) { uri: Uri? ->
                        lifecycle.coroutineScope.launch {
                            AndroidEvent.pushEvent(
                                event =
                                    AndroidEvent.PickedFile(
                                        uri = uri,
                                    ),
                            )
                        }
                    }

                val dirPicker =
                    rememberLauncherForActivityResult(contract = OpenDocumentTreeWithPermissionsContract()) { uri: Uri? ->
                        uri?.let {
                            val flags =
                                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION

                            val isUriPermissionPersisted =
                                contentResolver.persistedUriPermissions.any { uriPermission -> uriPermission.uri == uri }
                            if (isUriPermissionPersisted) {
                                contentResolver.releasePersistableUriPermission(uri, flags)
                            }
                            contentResolver.takePersistableUriPermission(uri, flags)
                        }
                        lifecycle.coroutineScope.launch {
                            AndroidEvent.pushEvent(AndroidEvent.PickedDirectory(uri = uri))
                        }
                    }

                viewModel<MainVM>(
                    factory =
                        viewModelFactory {
                            initializer {
                                MainVM(
                                    performAction = { androidAction ->
                                        when (androidAction) {
                                            AndroidAction.OpenFilePicker -> {
                                                importFilePicker.launch("application/json")
                                            }

                                            AndroidAction.OpenDirPicker -> {
                                                dirPicker.launch(null)
                                            }
                                        }
                                    },
                                )
                            }
                        },
                )
            }
        }
    }
}

class OpenDocumentTreeWithPermissionsContract : ActivityResultContracts.OpenDocumentTree() {
    override fun createIntent(
        context: Context,
        input: Uri?,
    ): Intent =
        super.createIntent(context, input).apply {
            listOf(
                Intent.FLAG_GRANT_PREFIX_URI_PERMISSION,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
                Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION,
                Intent.FLAG_GRANT_READ_URI_PERMISSION,
            ).forEach {
                addFlags(it)
            }
        }
}

private class MainVM(
    performAction: (AndroidAction) -> Unit,
) : ViewModel() {
    init {
        viewModelScope.launch {
            AndroidEvent.readEvents.collect {
                if (it is AndroidEvent.PickAFile) {
                    performAction(AndroidAction.OpenFilePicker)
                }
                if (it is AndroidEvent.PickADirectory) {
                    performAction(AndroidAction.OpenDirPicker)
                }
            }
        }
    }
}

private enum class AndroidAction {
    OpenFilePicker,
    OpenDirPicker,
}
