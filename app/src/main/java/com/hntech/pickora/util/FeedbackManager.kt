package com.hntech.pickora.util

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.hntech.pickora.data.ThemePreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Manages sound effects and haptic feedback for the app.
 * Uses ToneGenerator for low-latency tick/celebration sounds,
 * and system Vibrator for haptic feedback.
 *
 * Preference values are cached to avoid blocking the main thread.
 */
class FeedbackManager(
    private val context: Context,
    private val prefs: ThemePreferences
) {
    private val job = SupervisorJob()
    private val scope = CoroutineScope(job + Dispatchers.Main.immediate)

    // Cached preference values — updated reactively, never block main thread
    @Volatile private var soundEnabled = true
    @Volatile private var hapticEnabled = true

    init {
        scope.launch {
            prefs.themeConfig.collectLatest { config ->
                soundEnabled = config.soundEnabled
                hapticEnabled = config.hapticEnabled
            }
        }
    }

    private val vibrator: Vibrator by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val mgr = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            mgr.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    private val toneGenerator: ToneGenerator? by lazy {
        try {
            ToneGenerator(AudioManager.STREAM_MUSIC, 40)
        } catch (e: Exception) {
            null
        }
    }

    private val celebrateToneGenerator: ToneGenerator? by lazy {
        try {
            ToneGenerator(AudioManager.STREAM_MUSIC, 60)
        } catch (e: Exception) {
            null
        }
    }

    /** Light tick — called when wheel crosses a sector boundary */
    fun tick() {
        if (hapticEnabled) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(8, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(8)
                }
            } catch (_: Exception) {}
        }
        if (soundEnabled) {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 15)
            } catch (_: Exception) {}
        }
    }

    /** Medium haptic — for button presses, spin start */
    fun impact() {
        if (hapticEnabled) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(20)
                }
            } catch (_: Exception) {}
        }
        if (soundEnabled) {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 30)
            } catch (_: Exception) {}
        }
    }

    /** Celebration — strong double-tap for winner announcement */
    fun celebrate() {
        if (hapticEnabled) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val pattern = longArrayOf(0, 50, 80, 50, 80, 100)
                    vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(longArrayOf(0, 50, 80, 50, 80, 100), -1)
                }
            } catch (_: Exception) {}
        }
        if (soundEnabled) {
            try {
                celebrateToneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200)
            } catch (_: Exception) {}
        }
    }
}
