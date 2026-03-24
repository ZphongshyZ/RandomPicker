package com.hntech.pickora

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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.hntech.pickora.data.ThemeConfig
import com.hntech.pickora.data.ThemePreferences
import com.hntech.pickora.data.repository.ListRepository
import com.hntech.pickora.ui.components.AdBanner
import com.hntech.pickora.ui.navigation.Routes
import com.hntech.pickora.ui.navigation.PickoraNavGraph
import com.hntech.pickora.ui.theme.PickoraTheme
import org.koin.android.ext.android.get

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val themePreferences: ThemePreferences = get()
        val listRepository: ListRepository = get()

        setContent {
            val themeConfig by themePreferences.themeConfig.collectAsState(
                initial = ThemeConfig()
            )
            var pendingItems by remember { mutableStateOf<List<String>?>(null) }
            var pendingTarget by remember { mutableStateOf<String?>(null) }

            PickoraTheme(
                darkTheme = themeConfig.isDarkMode,
                dynamicColor = themeConfig.useDynamicColor
            ) {
                val navController = rememberNavController()
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route
                val adRoutes = setOf(
                    Routes.HOME,
                    Routes.HISTORY,
                    Routes.SAVED_LISTS,
                    Routes.SETTINGS
                )

                Scaffold(
                    bottomBar = {
                        if (currentRoute in adRoutes) {
                            AdBanner()
                        }
                    }
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
}
