package com.phongcha.pickora.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.runtime.rememberCoroutineScope
import com.phongcha.pickora.data.ThemePreferences
import com.phongcha.pickora.data.repository.ListRepository
import com.phongcha.pickora.data.repository.SavedList
import com.phongcha.pickora.ui.coinflip.CoinFlipScreen
import com.phongcha.pickora.ui.history.HistoryScreen
import com.phongcha.pickora.ui.home.HomeScreen
import com.phongcha.pickora.ui.namepicker.NamePickerScreen
import com.phongcha.pickora.ui.namepicker.NamePickerViewModel
import com.phongcha.pickora.ui.number.NumberScreen
import com.phongcha.pickora.ui.race.RaceScreen
import com.phongcha.pickora.ui.race.RaceViewModel
import com.phongcha.pickora.ui.savedlist.SavedListScreen
import com.phongcha.pickora.ui.settings.SettingsScreen
import com.phongcha.pickora.ui.wheel.WheelScreen
import com.phongcha.pickora.ui.wheel.WheelViewModel
import com.phongcha.pickora.ui.yesno.YesNoScreen
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

private const val ANIM_DURATION = 300

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
    onPendingConsumed: () -> Unit = {},
    onToggleLanguage: () -> Unit = {},
    currentLanguage: String = "en"
) {
    val scope = rememberCoroutineScope()

    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        modifier = modifier,
        enterTransition = {
            fadeIn(tween(ANIM_DURATION)) + scaleIn(
                initialScale = 0.94f,
                animationSpec = tween(ANIM_DURATION)
            )
        },
        exitTransition = {
            fadeOut(tween(ANIM_DURATION / 2))
        },
        popEnterTransition = {
            fadeIn(tween(ANIM_DURATION)) + scaleIn(
                initialScale = 0.94f,
                animationSpec = tween(ANIM_DURATION)
            )
        },
        popExitTransition = {
            fadeOut(tween(ANIM_DURATION / 2)) + scaleOut(
                targetScale = 1.04f,
                animationSpec = tween(ANIM_DURATION / 2)
            )
        }
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                onToggleLanguage = onToggleLanguage,
                currentLanguage = currentLanguage,
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
