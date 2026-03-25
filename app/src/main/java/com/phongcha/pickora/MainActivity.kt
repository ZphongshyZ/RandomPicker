package com.phongcha.pickora

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.phongcha.pickora.data.ThemeConfig
import com.phongcha.pickora.data.ThemePreferences
import com.phongcha.pickora.data.repository.ListRepository
import com.phongcha.pickora.ui.components.AdBanner
import com.phongcha.pickora.ui.navigation.PickoraNavGraph
import com.phongcha.pickora.util.FeedbackManager
import com.phongcha.pickora.data.dataStore
import com.phongcha.pickora.ui.theme.PickoraTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.get
import androidx.core.os.LocaleListCompat
import java.util.Locale

class MainActivity : ComponentActivity() {

    private lateinit var adView: AdView

    override fun attachBaseContext(newBase: Context) {
        val prefs = runBlocking { newBase.dataStore.data.first() }
        val langCode = prefs[androidx.datastore.preferences.core.stringPreferencesKey("language_code")] ?: ""
        if (langCode.isNotEmpty()) {
            val locale = Locale.forLanguageTag(langCode)
            val config = Configuration(newBase.resources.configuration).apply { setLocale(locale) }
            super.attachBaseContext(newBase.createConfigurationContext(config))
        } else {
            super.attachBaseContext(newBase)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Pre-create AdView once — never recreated during navigation
        val adUnitId = if (BuildConfig.DEBUG) {
            "ca-app-pub-3940256099942544/6300978111"
        } else {
            "ca-app-pub-4019714899445933/1522082453"
        }
        val adWidthDp = resources.configuration.screenWidthDp - 16
        val adSize = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidthDp)
        adView = AdView(this).apply {
            setAdSize(adSize)
            this.adUnitId = adUnitId
            loadAd(AdRequest.Builder().build())
        }

        val themePreferences: ThemePreferences = get()
        val listRepository: ListRepository = get()

        setContent {
            val themeConfig by themePreferences.themeConfig.collectAsState(
                initial = ThemeConfig()
            )
            val langScope = rememberCoroutineScope()
            var pendingItems by remember { mutableStateOf<List<String>?>(null) }
            var pendingTarget by remember { mutableStateOf<String?>(null) }

            PickoraTheme(
                darkTheme = themeConfig.isDarkMode,
                dynamicColor = themeConfig.useDynamicColor
            ) {
                val navController = rememberNavController()

                Scaffold(
                    bottomBar = { AdBanner(adView = adView) }
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        PickoraNavGraph(
                            navController = navController,
                            listRepository = listRepository,
                            themePreferences = themePreferences,
                            pendingItems = pendingItems,
                            pendingTarget = pendingTarget,
                            currentLanguage = themeConfig.languageCode.ifEmpty { "en" },
                            onToggleLanguage = {
                                val newLang = if (themeConfig.languageCode == "vi") "en" else "vi"
                                langScope.launch {
                                    themePreferences.setLanguageCode(newLang)
                                    this@MainActivity.recreate()
                                }
                            },
                            onPendingItemsSet = { items, target ->
                                pendingItems = items
                                pendingTarget = target
                            },
                            onPendingConsumed = {
                                pendingItems = null
                                pendingTarget = null
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        if (::adView.isInitialized) adView.destroy()
        try { get<FeedbackManager>().release() } catch (_: Exception) {}
        super.onDestroy()
    }
}
