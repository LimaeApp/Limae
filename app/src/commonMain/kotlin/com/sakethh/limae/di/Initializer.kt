package com.sakethh.limae.di

import com.sakethh.limae.platform.platformDatabaseModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

suspend fun initializeKoin(externalDependencies: KoinAppDeclaration? = null) {
    val platformDatabaseModule = platformDatabaseModule()
    startKoin {
        // the modules which are further used should be invoked first
        // eg: androidContext() from android platform should be
        // added to the Koin container and then the ones that
        // depend upon it can use it
        // else its just going to throw:
        // MissingAndroidContextException("Can't resolve Context instance. $ERROR_MSG")
        externalDependencies?.invoke(this)

        modules(
            platformDatabaseModule,
            notesModule,
            suggestionsModule,
            sharedDatabaseModule,
            utilsModule,
        )
    }
}
