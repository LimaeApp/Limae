package com.sakethh.limae

import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import com.sakethh.limae.di.initializeKoin
import com.sakethh.limae.platform.Platform
import com.sakethh.limae.utils.LimaePreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module

class LimaeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        runBlocking {
            initializeKoin {
                androidContext(this@LimaeApplication)
                modules(
                    module {
                        single {
                            object : Platform.Actions {
                                override fun openAccessibilitySettings() {
                                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    startActivity(intent)
                                }

                                override suspend fun getInstalledApps(): List<Platform.Actions.InstalledApp> =
                                    withContext(Dispatchers.IO) {
                                        applicationContext.packageManager
                                            .getInstalledApplications(
                                                PackageManager.GET_META_DATA,
                                            ).map {
                                                async {
                                                    Platform.Actions.InstalledApp(
                                                        name =
                                                            try {
                                                                packageManager
                                                                    .getApplicationLabel(it)
                                                                    .toString()
                                                            } catch (_: Exception) {
                                                                ""
                                                            },
                                                        packageName = it.packageName.toString(),
                                                    )
                                                }
                                            }.awaitAll()
                                    }
                            }
                        }.bind<Platform.Actions>()
                    },
                )
            }
            LimaePreferences.loadAll()
        }
    }
}
