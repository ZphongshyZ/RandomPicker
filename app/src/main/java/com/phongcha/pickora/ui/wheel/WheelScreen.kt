package com.phongcha.pickora.ui.wheel

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SuggestionChip
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.phongcha.pickora.R
import com.phongcha.pickora.domain.preset.PresetProvider
import com.phongcha.pickora.ui.components.ConfettiEffect
import com.phongcha.pickora.ui.components.ResultDialog
import com.phongcha.pickora.ui.components.SaveListDialog
import com.phongcha.pickora.ui.picker.BasePickerViewModel
import com.phongcha.pickora.util.FeedbackManager
import com.phongcha.pickora.util.ReviewHelper
import com.phongcha.pickora.util.ShareCardHelper
import com.phongcha.pickora.util.ShareHelper
import org.koin.androidx.compose.koinViewModel
import kotlin.math.cos
import kotlin.math.sin

private val SpinDeceleration = CubicBezierEasing(0.12f, 0.6f, 0.15f, 1f)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WheelScreen(
    onBack: () -> Unit,
    viewModel: WheelViewModel = koinViewModel()
) {
    val options by viewModel.options.collectAsState()
    val isAnimating by viewModel.isAnimating.collectAsState()
    val result by viewModel.result.collectAsState()
    val targetRotation by viewModel.targetRotation.collectAsState()
    val removeAfterPick by viewModel.removeAfterPick.collectAsState()
    val showConfetti by viewModel.showConfetti.collectAsState()
    val context = LocalContext.current
    val feedbackManager: FeedbackManager = remember {
        org.koin.java.KoinJavaComponent.getKoin().get()
    }

    var showEditSheet by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var showResultDialog by remember { mutableStateOf(false) }

    val rotation = remember { Animatable(0f) }
    var lastSectorIndex by remember { mutableStateOf(-1) }

    // Tick haptic when pointer crosses sector boundary
    LaunchedEffect(rotation.value, options.size) {
        if (options.isNotEmpty() && isAnimating) {
            val sectorAngle = 360f / options.size
            val currentSector = ((rotation.value % 360f) / sectorAngle).toInt()
            if (currentSector != lastSectorIndex) {
                lastSectorIndex = currentSector
                feedbackManager.tick()
            }
        }
    }

    // Drive spin animation
    LaunchedEffect(targetRotation) {
        if (targetRotation != 0f && isAnimating) {
            feedbackManager.impact()
            rotation.animateTo(
                targetValue = targetRotation,
                animationSpec = tween(durationMillis = 5000, easing = SpinDeceleration)
            )
            viewModel.onSpinFinished()
            feedbackManager.celebrate()
            ReviewHelper.recordPick(context)
            showResultDialog = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.wheel_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                actions = {
                    result?.let {
                        IconButton(onClick = {
                            ShareCardHelper.shareAsImage(context, it.label, context.getString(R.string.mode_spin_wheel), "\uD83C\uDFA1")
                        }) {
                            Icon(Icons.Default.Share, contentDescription = stringResource(R.string.action_share))
                        }
                    }
                }
            )
        }
    ) { padding ->
        // ════════════════════════════════════════
        //  PLAY MODE — wheel-centric layout
        // ════════════════════════════════════════
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // ── The Wheel ──
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .aspectRatio(1f, matchHeightConstraintsFirst = false)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Shadow layer behind wheel
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp)
                            .shadow(
                                elevation = 12.dp,
                                shape = CircleShape,
                                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            )
                    )

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val diameter = size.minDimension
                        val radius = diameter / 2f
                        val center = Offset(size.width / 2f, size.height / 2f)

                        if (options.isNotEmpty()) {
                            val totalWeight = options.sumOf { it.weight.toDouble() }.toFloat()
                            val sweepAngles = options.map { (it.weight / totalWeight) * 360f }
                            rotate(degrees = rotation.value, pivot = center) {
                                var cumulativeAngle = -90f
                                options.forEachIndexed { index, option ->
                                    val startAngle = cumulativeAngle
                                    val sweepAngle = sweepAngles[index]
                                    cumulativeAngle += sweepAngle

                                    // Sector fill
                                    drawArc(
                                        color = option.color,
                                        startAngle = startAngle,
                                        sweepAngle = sweepAngle,
                                        useCenter = true,
                                        topLeft = Offset(center.x - radius, center.y - radius),
                                        size = Size(diameter, diameter)
                                    )

                                    // Inner highlight (lighter edge near center)
                                    drawArc(
                                        color = Color.White.copy(alpha = 0.08f),
                                        startAngle = startAngle,
                                        sweepAngle = sweepAngle,
                                        useCenter = true,
                                        topLeft = Offset(center.x - radius * 0.35f, center.y - radius * 0.35f),
                                        size = Size(diameter * 0.35f, diameter * 0.35f)
                                    )

                                    // Sector divider line
                                    val lineAngle = Math.toRadians(startAngle.toDouble())
                                    drawLine(
                                        color = Color.White.copy(alpha = 0.9f),
                                        start = center,
                                        end = Offset(
                                            center.x + (radius * cos(lineAngle)).toFloat(),
                                            center.y + (radius * sin(lineAngle)).toFloat()
                                        ),
                                        strokeWidth = 2f
                                    )

                                    // Label
                                    val midAngle = Math.toRadians((startAngle + sweepAngle / 2f).toDouble())
                                    val labelRadius = radius * 0.65f
                                    val labelX = center.x + (labelRadius * cos(midAngle)).toFloat()
                                    val labelY = center.y + (labelRadius * sin(midAngle)).toFloat()
                                    val maxChars = when {
                                        options.size > 12 -> 5
                                        options.size > 8 -> 7
                                        options.size > 5 -> 9
                                        else -> 12
                                    }
                                    val textSz = (radius * 0.09f).coerceIn(13f, 36f) *
                                        when {
                                            options.size > 12 -> 0.7f
                                            options.size > 8 -> 0.8f
                                            else -> 1f
                                        }

                                    drawContext.canvas.nativeCanvas.apply {
                                        val paint = android.graphics.Paint().apply {
                                            color = android.graphics.Color.WHITE
                                            textSize = textSz
                                            textAlign = android.graphics.Paint.Align.CENTER
                                            typeface = android.graphics.Typeface.DEFAULT_BOLD
                                            isAntiAlias = true
                                            setShadowLayer(4f, 1f, 1f, android.graphics.Color.argb(120, 0, 0, 0))
                                        }
                                        val label = if (option.label.length > maxChars)
                                            option.label.take(maxChars - 1) + "\u2026"
                                        else option.label
                                        drawText(label, labelX, labelY + paint.textSize / 3f, paint)
                                    }
                                }
                            }
                        }

                        // Outer ring - double ring for depth
                        drawCircle(
                            color = Color.Black.copy(alpha = 0.12f),
                            radius = radius + 4f,
                            center = center,
                            style = Stroke(width = 3f)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = radius,
                            center = center,
                            style = Stroke(width = 8f)
                        )

                        // Center hub - layered for 3D effect
                        drawCircle(color = Color.White, radius = radius * 0.13f, center = center)
                        drawCircle(
                            color = Color.Black.copy(alpha = 0.06f),
                            radius = radius * 0.13f,
                            center = center,
                            style = Stroke(width = 2f)
                        )
                        drawCircle(color = Color(0xFF424242), radius = radius * 0.08f, center = center)
                        drawCircle(color = Color(0xFF616161), radius = radius * 0.05f, center = center)

                        // Pointer on RIGHT side — narrow triangle, sharp tip pointing left into wheel
                        val ps = radius * 0.20f
                        val baseHalf = ps * 0.35f
                        val baseX = center.x + radius + ps * 0.15f
                        val tipX = center.x + radius - ps * 1.1f

                        // Shadow
                        val shadowPath = Path().apply {
                            moveTo(baseX + 3f, center.y - baseHalf + 2f)
                            lineTo(baseX + 3f, center.y + baseHalf + 2f)
                            lineTo(tipX + 3f, center.y + 2f)
                            close()
                        }
                        drawPath(shadowPath, color = Color.Black.copy(alpha = 0.2f))

                        // Pointer
                        val pointerPath = Path().apply {
                            moveTo(baseX, center.y - baseHalf)
                            lineTo(baseX, center.y + baseHalf)
                            lineTo(tipX, center.y)
                            close()
                        }
                        drawPath(pointerPath, color = Color(0xFFD32F2F))
                        drawPath(pointerPath, color = Color.White.copy(alpha = 0.3f), style = Stroke(width = 1.5f))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ── Compact info bar ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.options_count, options.size),
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
                            Text(stringResource(R.string.wheel_edit_list), fontSize = 13.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ── Spin button ──
                Button(
                    onClick = {
                        viewModel.dismissConfetti()
                        viewModel.spin()
                    },
                    enabled = !isAnimating && options.size >= 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = if (isAnimating) stringResource(R.string.state_spinning)
                        else stringResource(R.string.action_spin),
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
            winnerLabelRes = R.string.wheel_winner_label,
            onDismiss = {
                showResultDialog = false
                viewModel.dismissConfetti()
            },
            onShare = {
                ShareCardHelper.shareAsImage(context, winner.label, context.getString(R.string.mode_spin_wheel), "\uD83C\uDFA1")
            },
            actionButtons = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                ) {
                    FilledTonalButton(
                        onClick = {
                            showResultDialog = false
                            viewModel.spinAgain()
                        },
                        enabled = options.size >= 2
                    ) {
                        Text(stringResource(R.string.wheel_spin_again), maxLines = 1)
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

    // ════════════════════════════════════════
    //  EDIT BOTTOM SHEET
    // ════════════════════════════════════════
    if (showEditSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        var newOptionText by remember { mutableStateOf("") }
        var pasteText by remember { mutableStateOf("") }
        var showPasteField by remember { mutableStateOf(false) }

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
                // Header
                Text(
                    text = stringResource(R.string.wheel_edit_list),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Add input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newOptionText,
                        onValueChange = { newOptionText = it },
                        placeholder = { Text(stringResource(R.string.wheel_add_option_hint)) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            if (newOptionText.isNotBlank()) {
                                viewModel.addOption(newOptionText.trim())
                                newOptionText = ""
                            }
                        }),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = {
                        if (newOptionText.isNotBlank()) {
                            viewModel.addOption(newOptionText.trim())
                            newOptionText = ""
                        }
                    }) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.cd_add))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Bulk paste
                if (!showPasteField) {
                    TextButton(onClick = { showPasteField = true }) {
                        Text(stringResource(R.string.paste_list))
                    }
                } else {
                    OutlinedTextField(
                        value = pasteText,
                        onValueChange = { pasteText = it },
                        placeholder = { Text(stringResource(R.string.paste_hint)) },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row {
                        FilledTonalButton(
                            onClick = {
                                viewModel.addBatchOptions(pasteText)
                                pasteText = ""
                                showPasteField = false
                            },
                            enabled = pasteText.isNotBlank()
                        ) { Text(stringResource(R.string.paste_add)) }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(onClick = { pasteText = ""; showPasteField = false }) {
                            Text(stringResource(R.string.saved_lists_cancel))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Templates row
                Text(
                    text = stringResource(R.string.home_quick_presets),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(PresetProvider.getAllPresets()) { preset ->
                        SuggestionChip(
                            onClick = { viewModel.loadOptionsFromStrings(preset.resolveItems(context)) },
                            label = { Text("${preset.emoji} ${stringResource(preset.nameRes)}", maxLines = 1, fontSize = 12.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Save button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.options_count, options.size),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    TextButton(onClick = { showSaveDialog = true }) {
                        Text(stringResource(R.string.wheel_save_list))
                    }
                }

                // Option list — numbered cards
                LazyColumn(
                    modifier = Modifier.height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    itemsIndexed(options, key = { _, opt -> opt.id }) { index, option ->
                        val sectorColors = BasePickerViewModel.sectorColors()
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = option.color.copy(alpha = 0.10f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                // Top row: number badge + label + delete
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier.size(28.dp).clip(CircleShape).background(option.color),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("${index + 1}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(option.label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    IconButton(onClick = { viewModel.removeOption(option.id) }, modifier = Modifier.size(28.dp)) {
                                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cd_remove), modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                // Color picker dots
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(stringResource(R.string.wheel_color), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    sectorColors.forEach { c ->
                                        Box(
                                            modifier = Modifier
                                                .size(if (c == option.color) 22.dp else 18.dp)
                                                .clip(CircleShape)
                                                .background(c)
                                                .then(
                                                    if (c == option.color) Modifier.padding(0.dp) // selected indicator via size
                                                    else Modifier
                                                )
                                                .clickable { viewModel.updateOptionColor(option.id, c) }
                                        )
                                    }
                                }
                                // Weight slider
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(stringResource(R.string.wheel_weight), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Slider(
                                        value = option.weight,
                                        onValueChange = { viewModel.updateOptionWeight(option.id, it) },
                                        valueRange = 0.5f..5f,
                                        steps = 8,
                                        modifier = Modifier.weight(1f).height(24.dp)
                                    )
                                    Text("${String.format("%.1f", option.weight)}x", style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(32.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // ── Save dialog (shared component) ──
    if (showSaveDialog) {
        SaveListDialog(
            titleRes = R.string.wheel_save_dialog_title,
            hintRes = R.string.wheel_save_dialog_hint,
            defaultName = stringResource(R.string.wheel_default_save_name),
            onSave = { name ->
                viewModel.saveCurrentList(name)
                showSaveDialog = false
            },
            onDismiss = { showSaveDialog = false }
        )
    }

    // ── Rate app prompt ──
    var showRateDialog by remember { mutableStateOf(false) }
    LaunchedEffect(result) {
        if (result != null && ReviewHelper.shouldShowPrompt(context)) {
            showRateDialog = true
        }
    }
    if (showRateDialog) {
        AlertDialog(
            onDismissRequest = {
                ReviewHelper.onDismissed(context)
                showRateDialog = false
            },
            title = { Text(stringResource(R.string.rate_title)) },
            text = { Text(stringResource(R.string.rate_message)) },
            confirmButton = {
                Button(onClick = {
                    ReviewHelper.onReviewed(context)
                    showRateDialog = false
                    // In production: use Google Play In-App Review API here
                }) { Text(stringResource(R.string.rate_yes)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    ReviewHelper.onDismissed(context)
                    showRateDialog = false
                }) { Text(stringResource(R.string.rate_later)) }
            }
        )
    }

    // Confetti — Popup overlay, always on top of everything including dialogs
    ConfettiEffect(trigger = showConfetti)
}
