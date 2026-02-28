package com.sakethh.limae.platform

import android.content.res.Configuration
import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.sakethh.limae.HarperJVMEngine
import com.sakethh.limae.LimaeDatabase
import com.sakethh.limae.domain.EngineSuggestion
import com.sakethh.limae.domain.LanguageToolEngineRepo
import com.sakethh.limae.service.ReadTextFieldAccessibilityService
import com.sakethh.limae.utils.Constants
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import okio.Path.Companion.toPath
import org.koin.android.ext.koin.androidContext
import org.koin.compose.getKoin
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

actual object HarperEngine : com.sakethh.limae.domain.HarperEngineRepo {
    actual override suspend fun checkText(text: String): List<EngineSuggestion> = HarperJVMEngine.checkText(text)
}

actual val platform: Platform
    get() =
        run {
            val configuration = Configuration()
            configuration.setToDefaults()
            if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                object : Platform {
                    override val version: Int = Build.VERSION.SDK_INT
                    override val type: Platform.Type
                        get() = Platform.Type.AndroidTablet
                }
            } else {
                object : Platform {
                    override val version: Int = Build.VERSION.SDK_INT
                    override val type: Platform.Type
                        get() = Platform.Type.AndroidMobile
                }
            }
        }

actual object LanguageToolEngine : LanguageToolEngineRepo {
    actual override suspend fun checkText(text: String): List<EngineSuggestion> = emptyList()
}

actual val LimaeIODispatcher: CoroutineDispatcher = Dispatchers.IO

actual suspend fun platformDatabaseModule(): Module =
    module {
        single {
            val driver =
                AndroidSqliteDriver(
                    schema = LimaeDatabase.Schema.synchronous(),
                    context = androidContext(),
                    name = Constants.DATABASE_NAME,
                )
            LimaeDatabase.invoke(driver)
        }.bind<LimaeDatabase>()
    }

@Composable
actual fun dynamicLightTheme(): ColorScheme =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        dynamicLightColorScheme(context = getKoin().get())
    } else {
        lightColorScheme()
    }

@Composable
actual fun dynamicDarkTheme(): ColorScheme =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        dynamicDarkColorScheme(context = getKoin().get())
    } else {
        darkColorScheme()
    }

actual val isReadTextFieldAccessibilityServiceRunning: Flow<Boolean> =
    ReadTextFieldAccessibilityService.connected
