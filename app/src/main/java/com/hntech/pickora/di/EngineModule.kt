package com.hntech.pickora.di

import com.hntech.pickora.data.ThemePreferences
import com.hntech.pickora.data.repository.HistoryRepository
import com.hntech.pickora.data.repository.ListRepository
import com.hntech.pickora.domain.engine.RandomEngine
import com.hntech.pickora.domain.engine.WeightedRandomEngine
import com.hntech.pickora.util.FeedbackManager
import com.hntech.pickora.ui.coinflip.CoinFlipViewModel
import com.hntech.pickora.ui.history.HistoryViewModel
import com.hntech.pickora.ui.home.HomeViewModel
import com.hntech.pickora.ui.namepicker.NamePickerViewModel
import com.hntech.pickora.ui.number.NumberViewModel
import com.hntech.pickora.ui.race.RaceViewModel
import com.hntech.pickora.ui.savedlist.SavedListViewModel
import com.hntech.pickora.ui.wheel.WheelViewModel
import com.hntech.pickora.ui.yesno.YesNoViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { ListRepository(get()) }
    single { HistoryRepository(get()) }
    single { ThemePreferences(get()) }
    single<RandomEngine> { WeightedRandomEngine() }
    single { FeedbackManager(androidContext(), get()) }

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
