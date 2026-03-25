package com.phongcha.pickora.ui.wheel

import androidx.lifecycle.viewModelScope
import android.content.Context
import com.phongcha.pickora.R
import com.phongcha.pickora.data.repository.HistoryRepository
import com.phongcha.pickora.data.repository.ListRepository
import com.phongcha.pickora.domain.engine.RandomEngine
import com.phongcha.pickora.domain.model.PickerOption
import com.phongcha.pickora.ui.picker.BasePickerViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

class WheelViewModel(
    randomEngine: RandomEngine,
    historyRepository: HistoryRepository,
    private val listRepository: ListRepository,
    context: Context
) : BasePickerViewModel(randomEngine, historyRepository) {

    override val pickerType = "wheel"

    init {
        val colors = sectorColors()
        val seeds = listOf(R.string.seed_wheel_1, R.string.seed_wheel_2, R.string.seed_wheel_3, R.string.seed_wheel_4, R.string.seed_wheel_5, R.string.seed_wheel_6)
        _options.value = seeds.mapIndexed { i, resId ->
            PickerOption("default_$i", context.getString(resId), colors[i % colors.size])
        }
    }

    private val _rotationDegrees = MutableStateFlow(0f)
    val rotationDegrees: StateFlow<Float> = _rotationDegrees.asStateFlow()

    private val _targetRotation = MutableStateFlow(0f)
    val targetRotation: StateFlow<Float> = _targetRotation.asStateFlow()

    // Snapshot of options at spin time — immune to edits during animation
    private var spinSnapshot: List<PickerOption> = emptyList()

    private fun computeSectorAngles(opts: List<PickerOption>): List<Float> {
        val totalWeight = opts.sumOf { it.weight.toDouble() }.toFloat()
        if (totalWeight <= 0f) {
            val equal = 360f / opts.size
            return opts.map { equal }
        }
        return opts.map { (it.weight / totalWeight) * 360f }
    }

    fun spin() {
        val currentOptions = _options.value
        if (currentOptions.size < 2 || _isAnimating.value) return

        _isAnimating.value = true
        _result.value = null
        spinSnapshot = currentOptions

        val winner = randomEngine.selectRandom(currentOptions)
        val winnerIndex = currentOptions.indexOf(winner)

        val sectorAngles = computeSectorAngles(currentOptions)
        // Calculate the center angle of the winning sector
        var sectorStart = 0f
        for (i in 0 until winnerIndex) sectorStart += sectorAngles[i]
        val sectorCenter = sectorStart + sectorAngles[winnerIndex] / 2f
        val jitter = Random.nextFloat() * sectorAngles[winnerIndex] * 0.6f - sectorAngles[winnerIndex] * 0.3f

        val fullSpins = (6 + Random.nextInt(4)) * 360f
        // Pointer is at the RIGHT (90° from top), so offset by 90°
        val target = _rotationDegrees.value + fullSpins + (360f - sectorCenter - 90f) + jitter

        _targetRotation.value = target
    }

    fun onSpinFinished() {
        if (spinSnapshot.isEmpty()) return

        val normalizedAngle = _targetRotation.value % 360f
        val sectorAngles = computeSectorAngles(spinSnapshot)

        // Find which sector the pointer (at right = 90° from top) lands in
        val pointerAngle = (360f - normalizedAngle + 90f) % 360f
        var cumulative = 0f
        var winnerIdx = 0
        for (i in spinSnapshot.indices) {
            cumulative += sectorAngles[i]
            if (pointerAngle < cumulative) {
                winnerIdx = i
                break
            }
        }

        val winner = spinSnapshot[winnerIdx.coerceIn(0, spinSnapshot.size - 1)]
        onResultSelected(winner)
        _rotationDegrees.value = _targetRotation.value
        _isAnimating.value = false
        spinSnapshot = emptyList()
    }

    fun removeWinner() {
        val winner = _result.value ?: return
        val stillPresent = _options.value.any { it.id == winner.id }
        if (stillPresent) {
            _options.value = _options.value.filter { it.id != winner.id }
        }
        _result.value = null
        _showConfetti.value = false
    }

    fun canSpin(): Boolean = _options.value.size >= 2 && !_isAnimating.value

    fun spinAgain() {
        dismissConfetti()
        _result.value = null
        if (_options.value.size >= 2) {
            spin()
        }
    }

    fun saveCurrentList(name: String) {
        val items = _options.value.map { it.label }
        if (items.isEmpty()) return
        viewModelScope.launch {
            val id = listRepository.saveList(name, items, "wheel")
            listRepository.markUsed(id, "wheel")
        }
    }
}
