package com.sakethh.limae

import android.app.Application
import com.sakethh.limae.di.initializeKoin
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.koin.androidContext

class LimaeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        runBlocking {
            initializeKoin {
                androidContext(this@LimaeApplication)
            }
        }
    }
}