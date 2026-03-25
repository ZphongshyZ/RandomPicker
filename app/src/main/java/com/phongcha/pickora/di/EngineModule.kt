package com.phongcha.pickora.di

import com.phongcha.pickora.data.ThemePreferences
import com.phongcha.pickora.data.repository.HistoryRepository
import com.phongcha.pickora.data.repository.ListRepository
import com.phongcha.pickora.domain.engine.RandomEngine
import com.phongcha.pickora.domain.engine.WeightedRandomEngine
import com.phongcha.pickora.util.FeedbackManager
import com.phongcha.pickora.util.InterstitialAdManager
import com.phongcha.pickora.ui.coinflip.CoinFlipViewModel
import com.phongcha.pickora.ui.history.HistoryViewModel
import com.phongcha.pickora.ui.home.HomeViewModel
import com.phongcha.pickora.ui.namepicker.NamePickerViewModel
import com.phongcha.pickora.ui.number.NumberViewModel
import com.phongcha.pickora.ui.race.RaceViewModel
import com.phongcha.pickora.ui.savedlist.SavedListViewModel
import com.phongcha.pickora.ui.wheel.WheelViewModel
import com.phongcha.pickora.ui.yesno.YesNoViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { ListRepository(get()) }
    single { HistoryRepository(get()) }
    single { ThemePreferences(get()) }
    single<RandomEngine> { WeightedRandomEngine() }
    single { FeedbackManager(androidContext(), get()) }
    single { InterstitialAdManager(androidContext()) }

    viewModel { HomeViewModel(get(), get()) }
    viewModel { WheelViewModel(get(), get(), get(), androidContext()) }
    viewModel { RaceViewModel(get(), get(), get(), androidContext()) }
    viewModel { NumberViewModel(get(), get()) }
    viewModel { YesNoViewModel(get(), get()) }
    viewModel { CoinFlipViewModel(get(), get()) }
    viewModel { NamePickerViewModel(get(), get(), get()) }
    viewModel { SavedListViewModel(get()) }
    viewModel { HistoryViewModel(get()) }
}
