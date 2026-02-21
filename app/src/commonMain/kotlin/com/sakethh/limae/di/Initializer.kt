package com.sakethh.limae.di

import com.sakethh.limae.platform.platformDatabaseModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

suspend fun initializeKoin(koinAppDeclaration: KoinAppDeclaration? = null) {
    val platformDatabaseModule = platformDatabaseModule()
    startKoin {
        koinAppDeclaration?.invoke(this)
        modules(
            notesModule,
            platformDatabaseModule,
            suggestionsModule,
            sharedDatabaseModule,
            platformUtilsModule
        )
    }
}