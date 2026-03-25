package com.phongcha.pickora

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.phongcha.pickora.di.appModule
import com.phongcha.pickora.util.ReviewHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class PickoraApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@PickoraApplication)
            modules(appModule)
        }

        // Initialize AdMob on background thread to avoid blocking app start
        CoroutineScope(Dispatchers.IO).launch {
            MobileAds.initialize(this@PickoraApplication) {}
        }

        ReviewHelper.incrementSession(this)
    }
}
