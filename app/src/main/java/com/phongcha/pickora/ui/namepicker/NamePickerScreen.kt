package com.phongcha.pickora.ui.namepicker

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.phongcha.pickora.R
import com.phongcha.pickora.ui.components.AddOptionInput
import com.phongcha.pickora.ui.components.ConfettiEffect
import com.phongcha.pickora.ui.components.OptionListItem
import com.phongcha.pickora.ui.components.ResultDialog
import com.phongcha.pickora.ui.components.SaveListDialog
import com.phongcha.pickora.util.ShareCardHelper
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NamePickerScreen(
    onBack: () -> Unit,
    viewModel: NamePickerViewModel = koinViewModel()
) {
    val options by viewModel.options.collectAsState()
    val isAnimating by viewModel.isAnimating.collectAsState()
    val result by viewModel.result.collectAsState()
    val highlightedIndex by viewModel.highlightedIndex.collectAsState()
    val removeAfterPick by viewModel.removeAfterPick.collectAsState()
    val showConfetti by viewModel.showConfetti.collectAsState()
    val context = LocalContext.current

    var showEditSheet by remember { mutableStateOf(false) }
    var showResultDialog by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }

    LaunchedEffect(result) {
        if (result != null) {
            showResultDialog = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.name_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                actions = {
                    result?.let {
                        IconButton(onClick = { ShareCardHelper.shareAsImage(context, it.label, context.getString(R.string.mode_name_picker), "\uD83D\uDC64") }) {
                            Icon(Icons.Default.Share, contentDescription = stringResource(R.string.action_share))
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (options.isEmpty() && !isAnimating) {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(text = "\uD83D\uDC64", fontSize = 56.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.name_empty_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.name_empty_sub),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    FilledTonalButton(onClick = { showEditSheet = true }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.name_edit))
                    }
                    Spacer(modifier = Modifier.weight(1f))
                } else {
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        itemsIndexed(options, key = { _, opt -> opt.id }) { index, option ->
                            val isHighlighted = index == highlightedIndex && isAnimating
                            val bgColor by animateColorAsState(
                                targetValue = if (isHighlighted) option.color.copy(alpha = 0.5f)
                                else option.color.copy(alpha = 0.08f),
                                animationSpec = tween(80),
                                label = "name_hl"
                            )

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = bgColor)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(option.color),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = option.label.take(1).uppercase(),
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = option.label,
                                        modifier = Modifier.weight(1f),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
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
                            text = stringResource(R.string.names_count, options.size),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = removeAfterPick,
                                onClick = { viewModel.toggleRemoveAfterPick() },
                                label = { Text(stringResource(R.string.remove_after_pick), fontSize = 12.sp, maxLines = 1) }
                            )
                            FilledTonalButton(
                                onClick = { showEditSheet = true },
                                enabled = !isAnimating
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(R.string.name_edit), fontSize = 13.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            viewModel.dismissConfetti()
                            viewModel.pickRandom()
                        },
                        enabled = !isAnimating && options.size >= 2,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = if (isAnimating) stringResource(R.string.state_picking)
                            else stringResource(R.string.action_pick_name),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

        }
    }

    // ── Result Dialog (shared component) ──
    if (showResultDialog && result != null) {
        val winner = result!!
        ResultDialog(
            winnerLabel = winner.label,
            winnerLabelRes = R.string.name_winner_label,
            onDismiss = {
                showResultDialog = false
                viewModel.dismissConfetti()
            },
            onShare = {
                ShareCardHelper.shareAsImage(context, winner.label, context.getString(R.string.mode_name_picker), "\uD83D\uDC64")
            },
            actionButtons = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                ) {
                    FilledTonalButton(
                        onClick = {
                            showResultDialog = false
                            viewModel.pickAgain()
                        },
                        enabled = options.size >= 2
                    ) {
                        Text(stringResource(R.string.name_pick_again), maxLines = 1)
                    }
                    if (!removeAfterPick && options.any { it.id == winner.id }) {
                        OutlinedButton(onClick = {
                            viewModel.removeWinner()
                            showResultDialog = false
                        }) {
                            Text(stringResource(R.string.action_remove), maxLines = 1)
                        }
                    }
                }
            }
        )
    }

    // ── Edit Bottom Sheet (using shared components) ──
    if (showEditSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        ModalBottomSheet(
            onDismissRequest = { showEditSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .navigationBarsPadding()
            ) {
                Text(
                    text = stringResource(R.string.name_edit),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                AddOptionInput(
                    placeholderRes = R.string.name_add_hint,
                    pasteLabelRes = R.string.name_paste_button,
                    pasteHintRes = R.string.name_paste_hint,
                    pasteAddRes = R.string.name_paste_add,
                    onAddSingle = { viewModel.addOption(it) },
                    onAddBatch = { viewModel.addBatchNames(it) }
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.names_count, options.size),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    TextButton(onClick = { showSaveDialog = true }) {
                        Text(stringResource(R.string.name_save_list))
                    }
                }

                LazyColumn(
                    modifier = Modifier.height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
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

    // ── Save dialog (shared component) ──
    if (showSaveDialog) {
        SaveListDialog(
            titleRes = R.string.name_save_dialog_title,
            hintRes = R.string.name_save_dialog_hint,
            defaultName = stringResource(R.string.name_default_save_name),
            onSave = { name ->
                viewModel.saveCurrentList(name)
                showSaveDialog = false
            },
            onDismiss = { showSaveDialog = false }
        )
    }

    // Confetti — Popup overlay, always on top of everything including dialogs
    ConfettiEffect(trigger = showConfetti)
}
