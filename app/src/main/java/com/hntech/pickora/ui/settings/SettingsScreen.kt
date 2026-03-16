package com.hntech.pickora.ui.settings

import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hntech.pickora.R
import com.hntech.pickora.data.ThemeConfig
import com.hntech.pickora.data.ThemePreferences
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    themePrefs: ThemePreferences
) {
    val config by themePrefs.themeConfig.collectAsState(initial = ThemeConfig())
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Appearance section
            SectionHeader(stringResource(R.string.settings_section_appearance))

            SettingsRow(
                label = stringResource(R.string.settings_dark_mode),
                checked = config.isDarkMode,
                onCheckedChange = { scope.launch { themePrefs.setDarkMode(it) } }
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                SettingsRow(
                    label = stringResource(R.string.settings_dynamic_color),
                    checked = config.useDynamicColor,
                    onCheckedChange = { scope.launch { themePrefs.setDynamicColor(it) } }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Feedback section
            SectionHeader(stringResource(R.string.settings_section_feedback))

            SettingsRow(
                label = stringResource(R.string.settings_sound),
                checked = config.soundEnabled,
                onCheckedChange = { scope.launch { themePrefs.setSoundEnabled(it) } }
            )

            SettingsRow(
                label = stringResource(R.string.settings_haptic),
                checked = config.hapticEnabled,
                onCheckedChange = { scope.launch { themePrefs.setHapticEnabled(it) } }
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun SettingsRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
