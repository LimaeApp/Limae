package com.sakethh.limae.platform

import android.content.res.Configuration
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.sakethh.limae.HarperJVMEngine
import com.sakethh.limae.LimaeDatabase
import com.sakethh.limae.domain.EngineSuggestion
import com.sakethh.limae.domain.LanguageToolEngineRepo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

actual object HarperEngine : com.sakethh.limae.domain.HarperEngineRepo {
    actual override suspend fun checkText(text: String): List<EngineSuggestion> {
        return HarperJVMEngine.checkText(text)
    }
}

actual val platform: Platform
    get() = run {
        val configuration = Configuration()
        configuration.setToDefaults()
        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) Platform.AndroidTablet else Platform.AndroidMobile
    }

actual object LanguageToolEngine : LanguageToolEngineRepo {
    actual override suspend fun checkText(text: String): List<EngineSuggestion> = emptyList()
}

actual val LimaeIODispatcher: CoroutineDispatcher = Dispatchers.IO

actual suspend fun platformDatabaseModule(): Module = module {
    single {
        val driver = AndroidSqliteDriver(
            schema = LimaeDatabase.Schema.synchronous(),
            context = androidContext(),
            name = "limae.db"
        )
        LimaeDatabase.invoke(driver)
    }.bind<LimaeDatabase>()
}