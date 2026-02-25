package com.sakethh.limae

import android.app.Application
import android.content.Intent
import android.provider.Settings
import com.sakethh.limae.di.initializeKoin
import com.sakethh.limae.platform.Platform
import com.sakethh.limae.utils.LimaePreferences
import kotlinx.coroutines.runBlocking
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
                            }
                        }.bind<Platform.Actions>()
                    },
                )
            }
            LimaePreferences.loadAll()
        }
    }
}
