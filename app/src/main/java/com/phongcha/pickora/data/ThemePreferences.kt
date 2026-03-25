package com.phongcha.pickora.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

internal val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_prefs")

data class ThemeConfig(
    val isDarkMode: Boolean = false,
    val useDynamicColor: Boolean = false,
    val wheelColorScheme: Int = 0,
    val soundEnabled: Boolean = true,
    val hapticEnabled: Boolean = true,
    val languageCode: String = ""
)

class ThemePreferences(private val context: Context) {

    private object Keys {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val WHEEL_COLOR_SCHEME = intPreferencesKey("wheel_color_scheme")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val HAPTIC_ENABLED = booleanPreferencesKey("haptic_enabled")
        val LANGUAGE_CODE = stringPreferencesKey("language_code")
    }

    val themeConfig: Flow<ThemeConfig> = context.dataStore.data
        .catch { emit(androidx.datastore.preferences.core.emptyPreferences()) }
        .map { prefs ->
            ThemeConfig(
                isDarkMode = prefs[Keys.DARK_MODE] ?: false,
                useDynamicColor = prefs[Keys.DYNAMIC_COLOR] ?: false,
                wheelColorScheme = prefs[Keys.WHEEL_COLOR_SCHEME] ?: 0,
                soundEnabled = prefs[Keys.SOUND_ENABLED] ?: true,
                hapticEnabled = prefs[Keys.HAPTIC_ENABLED] ?: true,
                languageCode = prefs[Keys.LANGUAGE_CODE] ?: ""
            )
        }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DARK_MODE] = enabled }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DYNAMIC_COLOR] = enabled }
    }

    suspend fun setWheelColorScheme(scheme: Int) {
        context.dataStore.edit { it[Keys.WHEEL_COLOR_SCHEME] = scheme }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SOUND_ENABLED] = enabled }
    }

    suspend fun setHapticEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.HAPTIC_ENABLED] = enabled }
    }

    suspend fun setLanguageCode(code: String) {
        context.dataStore.edit { it[Keys.LANGUAGE_CODE] = code }
    }
}
