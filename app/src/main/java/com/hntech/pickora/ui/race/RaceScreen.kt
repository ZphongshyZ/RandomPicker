package com.hntech.pickora.ui.race

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hntech.pickora.R
import com.hntech.pickora.ui.components.AddOptionInput
import com.hntech.pickora.ui.components.ConfettiEffect
import com.hntech.pickora.ui.components.OptionListItem
import com.hntech.pickora.ui.components.ResultDialog
import com.hntech.pickora.ui.components.SaveListDialog
import com.hntech.pickora.util.ShareCardHelper
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RaceScreen(
    onBack: () -> Unit,
    viewModel: RaceViewModel = koinViewModel()
) {
    val racers by viewModel.racers.collectAsState()
    val isAnimating by viewModel.isAnimating.collectAsState()
    val result by viewModel.result.collectAsState()
    val options by viewModel.options.collectAsState()
    val showConfetti by viewModel.showConfetti.collectAsState()
    val context = LocalContext.current

    var showEditSheet by remember { mutableStateOf(false) }
    var showResultDialog by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }

    LaunchedEffect(result) {
        if (result != null) showResultDialog = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.race_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                actions = {
                    result?.let {
                        IconButton(onClick = { ShareCardHelper.shareAsImage(context, it.label, context.getString(R.string.mode_animal_race), "\uD83C\uDFC6") }) {
                            Icon(Icons.Default.Share, contentDescription = stringResource(R.string.action_share))
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val animalEmojis = listOf("\uD83D\uDC15", "\uD83D\uDC08", "\uD83D\uDC07", "\uD83D\uDC22", "\uD83D\uDC0E", "\uD83D\uDC18", "\uD83E\uDD8A", "\uD83D\uDC3B", "\uD83D\uDC3C", "\uD83E\uDD81")
                    if (racers.isEmpty()) {
                        itemsIndexed(options, key = { _, opt -> opt.id }) { index, option ->
                            RaceLane(option.label, animalEmojis[index % 10], 0f, option.color)
                        }
                    } else {
                        itemsIndexed(racers, key = { _, r -> r.option.id }) { _, racer ->
                            RaceLane(racer.option.label, racer.emoji, racer.progress, racer.option.color)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.racers_count, options.size),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FilledTonalButton(onClick = { showEditSheet = true }, enabled = !isAnimating) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.race_edit), fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        viewModel.dismissConfetti()
                        viewModel.startRace()
                    },
                    enabled = !isAnimating && options.size >= 2,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = if (isAnimating) stringResource(R.string.state_racing) else stringResource(R.string.action_start_race),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

        }
    }

    // ── Result Dialog (shared component) ──
    if (showResultDialog && result != null) {
        val winner = result!!
        ResultDialog(
            winnerLabel = winner.label,
            winnerLabelRes = R.string.race_winner_label,
            emoji = "\uD83C\uDFC6",
            onDismiss = { showResultDialog = false; viewModel.dismissConfetti() },
            onShare = {
                ShareCardHelper.shareAsImage(context, winner.label, context.getString(R.string.mode_animal_race), "\uD83C\uDFC6")
            },
            actionButtons = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                ) {
                    FilledTonalButton(onClick = { showResultDialog = false; viewModel.raceAgain() }, enabled = options.size >= 2) {
                        Text(stringResource(R.string.race_again))
                    }
                    OutlinedButton(onClick = { showResultDialog = false; viewModel.resetRace() }) {
                        Text(stringResource(R.string.action_reset))
                    }
                }
            }
        )
    }

    // ── Edit Bottom Sheet (using shared components) ──
    if (showEditSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        ModalBottomSheet(onDismissRequest = { showEditSheet = false }, sheetState = sheetState) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).navigationBarsPadding()) {
                Text(stringResource(R.string.race_edit), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))

                AddOptionInput(
                    placeholderRes = R.string.race_add_hint,
                    pasteLabelRes = R.string.paste_list,
                    pasteHintRes = R.string.paste_hint,
                    pasteAddRes = R.string.paste_add,
                    onAddSingle = { viewModel.addOption(it) },
                    onAddBatch = { viewModel.addBatchOptions(it) }
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.racers_count, options.size), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    TextButton(onClick = { showSaveDialog = true }) { Text(stringResource(R.string.race_save_roster)) }
                }

                LazyColumn(modifier = Modifier.height(300.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    itemsIndexed(options, key = { _, opt -> opt.id }) { index, option ->
                        OptionListItem(
                            index = index,
                            option = option,
                            onRemove = { viewModel.removeOption(option.id) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // ── Save Dialog (shared component) ──
    if (showSaveDialog) {
        SaveListDialog(
            titleRes = R.string.race_save_dialog_title,
            hintRes = R.string.race_save_dialog_hint,
            defaultName = stringResource(R.string.race_default_save_name),
            onSave = { name ->
                viewModel.saveRoster(name)
                showSaveDialog = false
            },
            onDismiss = { showSaveDialog = false }
        )
    }

    // Confetti — Popup overlay, always on top of everything including dialogs
    ConfettiEffect(trigger = showConfetti)
}

@Composable
private fun RaceLane(label: String, emoji: String, progress: Float, color: Color) {
    val animatedProgress by animateFloatAsState(targetValue = progress, animationSpec = tween(durationMillis = 80), label = "race_progress")

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(2.dp))
        Box(
            modifier = Modifier.fillMaxWidth().height(40.dp).clip(RoundedCornerShape(8.dp)).background(color.copy(alpha = 0.15f))
        ) {
            Box(modifier = Modifier.fillMaxWidth(animatedProgress.coerceIn(0f, 1f)).height(40.dp).clip(RoundedCornerShape(8.dp)).background(color.copy(alpha = 0.4f)))
            Box(modifier = Modifier.align(Alignment.CenterEnd).width(3.dp).height(40.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)))
            Box(modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp), contentAlignment = Alignment.CenterStart) {
                val screenWidth = LocalConfiguration.current.screenWidthDp.dp - 80.dp
                val offsetX = screenWidth * animatedProgress.coerceIn(0f, 0.95f)
                Text(text = emoji, fontSize = 24.sp, modifier = Modifier.offset(x = offsetX))
            }
        }
    }
}
