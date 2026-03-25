package com.phongcha.pickora.ui.race

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.phongcha.pickora.R
import com.phongcha.pickora.data.repository.HistoryRepository
import com.phongcha.pickora.data.repository.ListRepository
import com.phongcha.pickora.domain.engine.RandomEngine
import com.phongcha.pickora.domain.model.PickerOption
import com.phongcha.pickora.ui.picker.BasePickerViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

data class RacerState(
    val option: PickerOption,
    val progress: Float = 0f,
    val emoji: String = ""
)

class RaceViewModel(
    randomEngine: RandomEngine,
    historyRepository: HistoryRepository,
    private val listRepository: ListRepository,
    context: Context
) : BasePickerViewModel(randomEngine, historyRepository) {

    override val pickerType = "race"

    private val animalEmojis = listOf("\uD83D\uDC15", "\uD83D\uDC08", "\uD83D\uDC07", "\uD83D\uDC22", "\uD83D\uDC0E", "\uD83D\uDC18", "\uD83E\uDD8A", "\uD83D\uDC3B", "\uD83D\uDC3C", "\uD83E\uDD81")

    private val _racers = MutableStateFlow<List<RacerState>>(emptyList())
    val racers: StateFlow<List<RacerState>> = _racers.asStateFlow()

    private var raceJob: Job? = null

    init {
        val colors = sectorColors()
        val seeds = listOf(R.string.seed_player_1, R.string.seed_player_2, R.string.seed_player_3, R.string.seed_player_4)
        _options.value = seeds.mapIndexed { i, resId ->
            PickerOption("racer_$i", context.getString(resId), colors[i % colors.size])
        }
    }

    fun startRace() {
        val currentOptions = _options.value
        if (currentOptions.size < 2 || _isAnimating.value) return

        _isAnimating.value = true
        _result.value = null

        _racers.value = currentOptions.mapIndexed { index, option ->
            RacerState(option = option, progress = 0f, emoji = animalEmojis[index % animalEmojis.size])
        }

        val winner = randomEngine.selectRandom(currentOptions)
        val winnerIndex = currentOptions.indexOf(winner)

        raceJob = viewModelScope.launch {
            val tickInterval = 50L
            val totalTicks = 80 + Random.nextInt(40)
            val baseSpeeds = currentOptions.indices.map {
                if (it == winnerIndex) 1.0f / totalTicks * 1.15f
                else 1.0f / totalTicks * (0.7f + Random.nextFloat() * 0.35f)
            }

            for (tick in 0 until totalTicks) {
                val currentRacers = _racers.value.toMutableList()
                var raceFinished = false
                for (i in currentRacers.indices) {
                    val tickVariation = 0.5f + Random.nextFloat() * 1.0f
                    val newProgress = (currentRacers[i].progress + baseSpeeds[i] * tickVariation).coerceAtMost(1f)
                    currentRacers[i] = currentRacers[i].copy(progress = newProgress)
                    if (newProgress >= 1f) raceFinished = true
                }
                _racers.value = currentRacers
                if (raceFinished) break
                delay(tickInterval)
            }

            val finalRacers = _racers.value.toMutableList()
            finalRacers[winnerIndex] = finalRacers[winnerIndex].copy(progress = 1f)
            _racers.value = finalRacers

            onResultSelected(winner)
            _isAnimating.value = false
        }
    }

    fun resetRace() {
        raceJob?.cancel()
        _racers.value = emptyList()
        _result.value = null
        _showConfetti.value = false
        _isAnimating.value = false
    }

    fun raceAgain() {
        dismissConfetti()
        _result.value = null
        _racers.value = emptyList()
        if (_options.value.size >= 2) startRace()
    }

    fun saveRoster(name: String) {
        val items = _options.value.map { it.label }
        if (items.isEmpty()) return
        viewModelScope.launch {
            val id = listRepository.saveList(name, items, "race")
            listRepository.markUsed(id, "race")
        }
    }

    override fun onCleared() {
        super.onCleared()
        raceJob?.cancel()
    }
}
