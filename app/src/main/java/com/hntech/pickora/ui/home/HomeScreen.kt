package com.hntech.pickora.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hntech.pickora.R
import com.hntech.pickora.data.repository.HistoryEntry
import com.hntech.pickora.domain.preset.Preset
import com.hntech.pickora.domain.preset.PresetProvider
import com.hntech.pickora.ui.picker.BasePickerViewModel
import com.hntech.pickora.util.ResultLocalizer
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigate: (String) -> Unit,
    onPresetSelected: (Preset) -> Unit,
    onLoadItems: (List<String>, String) -> Unit = { _, _ -> },
    onNavigateToHistory: () -> Unit,
    onNavigateToSavedLists: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    viewModel: HomeViewModel = koinViewModel()
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val recentHistory by viewModel.recentPicks.collectAsState(initial = emptyList())
    val lastUsedList by viewModel.lastUsedList.collectAsState(initial = null)
    val presets = PresetProvider.getAllPresets()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.home_tagline),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings_title))
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ── Hero CTA — dynamic for returning users ──
            item {
                val savedList = lastUsedList
                if (savedList != null) {
                    ContinueCard(
                        listName = savedList.name,
                        itemCount = savedList.items.size,
                        mode = savedList.preferredMode,
                        onClick = {
                            val route = when (savedList.preferredMode) {
                                "name" -> "name"
                                "race" -> "race"
                                else -> "wheel"
                            }
                            onLoadItems(savedList.items, route)
                        }
                    )
                } else {
                    // First time: standard wheel hero
                    HeroCard(onClick = { onNavigate("wheel") })
                }
            }

            // ── Recent picks ──
            if (recentHistory.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(R.string.home_recent_pick),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            recentHistory.forEach { entry ->
                                RecentPickChip(entry, onClick = { onNavigate(entry.pickerType) }, modifier = Modifier.weight(1f))
                            }
                            repeat(3 - recentHistory.size) { Spacer(modifier = Modifier.weight(1f)) }
                        }
                    }
                }
            }

            // ── Use-case blocks: For Groups ──
            item {
                Text(
                    text = stringResource(R.string.home_for_groups),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    item {
                        UseCaseCard(
                            emoji = "\uD83D\uDC64",
                            question = stringResource(R.string.home_use_case_who_first),
                            onClick = { onNavigate("name") }
                        )
                    }
                    item {
                        UseCaseCard(
                            emoji = "\uD83C\uDF54",
                            question = stringResource(R.string.home_use_case_what_eat),
                            onClick = {
                                presets.find { it.id == "what_to_eat" }?.let { onPresetSelected(it) }
                            }
                        )
                    }
                    item {
                        UseCaseCard(
                            emoji = "\uD83D\uDCB0",
                            question = stringResource(R.string.home_use_case_who_pays),
                            onClick = {
                                presets.find { it.id == "who_pays" }?.let { onPresetSelected(it) }
                            }
                        )
                    }
                    item {
                        UseCaseCard(
                            emoji = "\uD83C\uDFC1",
                            question = stringResource(R.string.mode_animal_race),
                            onClick = { onNavigate("race") }
                        )
                    }
                }
            }

            // ── Quick Decisions ──
            item {
                Text(
                    text = stringResource(R.string.home_quick_decisions),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    UseCaseCard(
                        emoji = "\u2753",
                        question = stringResource(R.string.home_use_case_yes_or_no),
                        onClick = { onNavigate("yesno") },
                        modifier = Modifier.weight(1f)
                    )
                    UseCaseCard(
                        emoji = "\uD83C\uDFB2",
                        question = stringResource(R.string.home_use_case_pick_number),
                        onClick = { onNavigate("number") },
                        modifier = Modifier.weight(1f)
                    )
                    UseCaseCard(
                        emoji = "\uD83E\uDE99",
                        question = stringResource(R.string.home_use_case_flip_coin),
                        onClick = { onNavigate("coinflip") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ── All modes (compact) ──
            item {
                Text(
                    text = stringResource(R.string.home_all_modes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val modes = listOf(
                        Triple("wheel", "\uD83C\uDFA1", R.string.mode_spin_wheel),
                        Triple("name", "\uD83D\uDC64", R.string.mode_name_picker),
                        Triple("number", "\uD83C\uDFB2", R.string.mode_random_number),
                        Triple("yesno", "\u2753", R.string.mode_yes_no),
                        Triple("coinflip", "\uD83E\uDE99", R.string.mode_coin_flip),
                        Triple("race", "\uD83C\uDFC1", R.string.mode_animal_race)
                    )
                    items(modes) { (route, emoji, nameRes) ->
                        SuggestionChip(
                            onClick = { onNavigate(route) },
                            label = { Text("$emoji ${stringResource(nameRes)}", maxLines = 1) }
                        )
                    }
                }
            }

            // ── Quick actions ──
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        SuggestionChip(
                            onClick = onNavigateToHistory,
                            label = { Text("\uD83D\uDCCB ${stringResource(R.string.home_history)}", maxLines = 1) }
                        )
                    }
                    item {
                        SuggestionChip(
                            onClick = onNavigateToSavedLists,
                            label = { Text("\uD83D\uDCBE ${stringResource(R.string.home_saved_lists)}", maxLines = 1) }
                        )
                    }
                }
            }

            // ── Presets ──
            item {
                Text(
                    text = stringResource(R.string.home_quick_presets),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(end = 16.dp)
                ) {
                    items(presets) { preset ->
                        PresetChip(preset = preset, onClick = { onPresetSelected(preset) })
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

// ── Use-case card: answers a real question ──
@Composable
private fun UseCaseCard(
    emoji: String,
    question: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.heightIn(min = 80.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = emoji, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = question,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ── Continue card for returning users ──
@Composable
private fun ContinueCard(listName: String, itemCount: Int, mode: String = "wheel", onClick: () -> Unit) {
    val modeEmoji = when (mode) {
        "name" -> "\uD83D\uDC64"
        "race" -> "\uD83C\uDFC1"
        else -> "\uD83C\uDFA1"
    }
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.home_continue_with),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.6f),
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = listName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${stringResource(R.string.home_preset_items_count, itemCount)} \u2022 ${stringResource(R.string.home_tap_to_open)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                )
            }
            Text(text = modeEmoji, fontSize = 40.sp, modifier = Modifier.padding(start = 12.dp))
        }
    }
}

// ── Signature hero card ──
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HeroCard(onClick: () -> Unit) {
    val primary = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary
    val onContainer = MaterialTheme.colorScheme.onPrimaryContainer
    val sectorColors = BasePickerViewModel.sectorColors()

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.radialGradient(
                            colors = listOf(tertiary.copy(alpha = 0.10f), primary.copy(alpha = 0.06f), Color.Transparent),
                            center = Offset(Float.POSITIVE_INFINITY, 0f),
                            radius = 600f
                        )
                    )
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.home_quick_start),
                        style = MaterialTheme.typography.labelLarge,
                        color = onContainer.copy(alpha = 0.55f),
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.mode_spin_wheel),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = onContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.home_hero_cta),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = primary
                    )
                }
                Canvas(modifier = Modifier.size(80.dp).padding(4.dp)) {
                    val radius = size.minDimension / 2f
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val sweep = 360f / 8
                    for (i in 0 until 8) {
                        drawArc(
                            color = sectorColors[i % sectorColors.size].copy(alpha = 0.85f),
                            startAngle = i * sweep - 90f, sweepAngle = sweep, useCenter = true,
                            topLeft = Offset(center.x - radius, center.y - radius),
                            size = Size(radius * 2, radius * 2)
                        )
                    }
                    for (i in 0 until 8) {
                        val angle = Math.toRadians((i * sweep - 90f).toDouble())
                        drawLine(
                            Color.White.copy(alpha = 0.6f), center,
                            Offset(center.x + (radius * kotlin.math.cos(angle)).toFloat(), center.y + (radius * kotlin.math.sin(angle)).toFloat()),
                            strokeWidth = 1.5f
                        )
                    }
                    drawCircle(onContainer.copy(alpha = 0.25f), radius, center, style = Stroke(width = 3f))
                    drawCircle(Color.White, radius * 0.15f, center)
                    drawCircle(onContainer.copy(alpha = 0.6f), radius * 0.08f, center)
                    // Pointer on RIGHT side, sharp tip pointing inward
                    val ps = radius * 0.20f
                    val baseHalf = ps * 0.35f
                    val baseX = center.x + radius + ps * 0.15f
                    val tipX = center.x + radius - ps * 1.1f
                    val pp = androidx.compose.ui.graphics.Path().apply {
                        moveTo(baseX, center.y - baseHalf)
                        lineTo(baseX, center.y + baseHalf)
                        lineTo(tipX, center.y)
                        close()
                    }
                    drawPath(pp, color = primary)
                }
            }
        }
    }
}

@Composable
private fun RecentPickChip(entry: HistoryEntry, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val typeEmoji = when (entry.pickerType) {
        "wheel" -> "\uD83C\uDFA1"; "number" -> "\uD83C\uDFB2"; "name" -> "\uD83D\uDC64"
        "yesno" -> "\u2753"; "coinflip" -> "\uD83E\uDE99"; "race" -> "\uD83C\uDFC1"
        else -> "\uD83C\uDFAF"
    }
    Card(
        onClick = onClick, modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = typeEmoji, fontSize = 16.sp)
            val displayResult = ResultLocalizer.localize(LocalContext.current, entry.result, entry.pickerType)
            Text(
                text = displayResult, style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun PresetChip(preset: Preset, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(text = preset.emoji, fontSize = 22.sp)
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = stringResource(preset.nameRes), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = stringResource(R.string.home_preset_items_count, preset.itemResIds.size), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f))
            }
        }
    }
}

