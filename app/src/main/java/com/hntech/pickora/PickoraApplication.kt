package com.hntech.pickora

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.hntech.pickora.di.appModule
import com.hntech.pickora.util.ReviewHelper
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class PickoraApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        
        // Initialize AdMob
        MobileAds.initialize(this) {}

        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@PickoraApplication)
            modules(appModule)
        }
        ReviewHelper.incrementSession(this)
    }
}
