package com.hntech.pickora.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.runtime.rememberCoroutineScope
import com.hntech.pickora.data.ThemePreferences
import com.hntech.pickora.data.repository.ListRepository
import com.hntech.pickora.data.repository.SavedList
import com.hntech.pickora.ui.coinflip.CoinFlipScreen
import com.hntech.pickora.ui.history.HistoryScreen
import com.hntech.pickora.ui.home.HomeScreen
import com.hntech.pickora.ui.namepicker.NamePickerScreen
import com.hntech.pickora.ui.namepicker.NamePickerViewModel
import com.hntech.pickora.ui.number.NumberScreen
import com.hntech.pickora.ui.race.RaceScreen
import com.hntech.pickora.ui.race.RaceViewModel
import com.hntech.pickora.ui.savedlist.SavedListScreen
import com.hntech.pickora.ui.settings.SettingsScreen
import com.hntech.pickora.ui.wheel.WheelScreen
import com.hntech.pickora.ui.wheel.WheelViewModel
import com.hntech.pickora.ui.yesno.YesNoScreen
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

object Routes {
    const val HOME = "home"
    const val WHEEL = "wheel"
    const val RACE = "race"
    const val NUMBER = "number"
    const val YES_NO = "yesno"
    const val COIN_FLIP = "coinflip"
    const val NAME_PICKER = "name"
    const val HISTORY = "history"
    const val SAVED_LISTS = "saved_lists"
    const val SETTINGS = "settings"
}

@Composable
fun PickoraNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    themePreferences: ThemePreferences? = null,
    listRepository: ListRepository? = null,
    pendingItems: List<String>? = null,
    pendingTarget: String? = null,
    onPendingItemsSet: (List<String>, String) -> Unit = { _, _ -> },
    onPendingConsumed: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()

    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        modifier = modifier
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                onNavigate = { route -> navController.navigate(route) },
                onPresetSelected = { preset ->
                    val route = when (preset.targetRoute) {
                        "name" -> Routes.NAME_PICKER
                        "race" -> Routes.RACE
                        else -> Routes.WHEEL
                    }
                    val ctx = navController.context
                    onPendingItemsSet(preset.resolveItems(ctx), route)
                    navController.navigate(route)
                },
                onLoadItems = { items, target ->
                    val route = when (target) {
                        "name" -> Routes.NAME_PICKER
                        "race" -> Routes.RACE
                        else -> Routes.WHEEL
                    }
                    onPendingItemsSet(items, route)
                    navController.navigate(route)
                },
                onNavigateToHistory = { navController.navigate(Routes.HISTORY) },
                onNavigateToSavedLists = { navController.navigate(Routes.SAVED_LISTS) },
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }
        composable(Routes.WHEEL) {
            val viewModel: WheelViewModel = koinViewModel()

            LaunchedEffect(pendingItems) {
                if (!pendingItems.isNullOrEmpty() && pendingTarget == Routes.WHEEL) {
                    viewModel.loadOptionsFromStrings(pendingItems)
                    onPendingConsumed()
                }
            }

            WheelScreen(onBack = { navController.popBackStack() }, viewModel = viewModel)
        }
        composable(Routes.NAME_PICKER) {
            val viewModel: NamePickerViewModel = koinViewModel()

            LaunchedEffect(pendingItems) {
                if (!pendingItems.isNullOrEmpty() && pendingTarget == Routes.NAME_PICKER) {
                    viewModel.loadOptionsFromStrings(pendingItems)
                    onPendingConsumed()
                }
            }

            NamePickerScreen(onBack = { navController.popBackStack() }, viewModel = viewModel)
        }
        composable(Routes.RACE) {
            val viewModel: RaceViewModel = koinViewModel()

            LaunchedEffect(pendingItems) {
                if (!pendingItems.isNullOrEmpty() && pendingTarget == Routes.RACE) {
                    viewModel.loadOptionsFromStrings(pendingItems)
                    onPendingConsumed()
                }
            }

            RaceScreen(onBack = { navController.popBackStack() }, viewModel = viewModel)
        }
        composable(Routes.NUMBER) {
            NumberScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.YES_NO) {
            YesNoScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.COIN_FLIP) {
            CoinFlipScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.HISTORY) {
            HistoryScreen(
                onBack = { navController.popBackStack() },
                onRunAgain = { items, pickerType ->
                    val route = when (pickerType) {
                        "name" -> Routes.NAME_PICKER
                        "race" -> Routes.RACE
                        else -> Routes.WHEEL
                    }
                    onPendingItemsSet(items, route)
                    navController.popBackStack()
                    navController.navigate(route)
                }
            )
        }
        composable(Routes.SETTINGS) {
            themePreferences?.let { prefs ->
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    themePrefs = prefs
                )
            }
        }
        composable(Routes.SAVED_LISTS) {
            SavedListScreen(
                onBack = { navController.popBackStack() },
                onListSelected = { list, target ->
                    val route = when (target) {
                        "name" -> Routes.NAME_PICKER
                        "race" -> Routes.RACE
                        else -> Routes.WHEEL
                    }
                    scope.launch { listRepository?.markUsed(list.id, target) }
                    onPendingItemsSet(list.items, route)
                    navController.popBackStack()
                    navController.navigate(route)
                }
            )
        }
    }
}
