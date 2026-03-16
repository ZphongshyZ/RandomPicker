package com.hntech.pickora.ui.number

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hntech.pickora.R
import com.hntech.pickora.ui.components.ConfettiEffect
import com.hntech.pickora.util.ShareHelper
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NumberScreen(
    onBack: () -> Unit,
    viewModel: NumberViewModel = koinViewModel()
) {
    val minValue by viewModel.minValue.collectAsState()
    val maxValue by viewModel.maxValue.collectAsState()
    val displayNumber by viewModel.displayNumber.collectAsState()
    val isAnimating by viewModel.isAnimating.collectAsState()
    val history by viewModel.history.collectAsState()
    val showConfetti by viewModel.showConfetti.collectAsState()
    val context = LocalContext.current

    var minText by remember { mutableStateOf(minValue.toString()) }
    var maxText by remember { mutableStateOf(maxValue.toString()) }

    val numberColor by animateColorAsState(
        targetValue = if (isAnimating) MaterialTheme.colorScheme.tertiary
        else MaterialTheme.colorScheme.primary,
        animationSpec = tween(200),
        label = "num_color"
    )
    val numberScale by animateFloatAsState(
        targetValue = if (isAnimating) 1.1f else 1f,
        animationSpec = tween(150),
        label = "num_scale"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.number_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                actions = {
                    displayNumber?.let { num ->
                        IconButton(onClick = { ShareHelper.shareResult(context, num.toString(), "number") }) {
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
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.weight(0.3f))

                // Lucky draw number display
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(32.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                        .padding(horizontal = 48.dp, vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = displayNumber?.toString() ?: stringResource(R.string.number_placeholder),
                        fontSize = 88.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = numberColor,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.scale(numberScale)
                    )
                }

                Spacer(modifier = Modifier.weight(0.2f))

                // Range inputs — compact card style
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = minText,
                        onValueChange = { value ->
                            minText = value
                            value.toIntOrNull()?.let { viewModel.setRange(it, maxValue) }
                        },
                        label = { Text(stringResource(R.string.label_min)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.width(110.dp),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Text(
                        text = " ${stringResource(R.string.label_to)} ",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    OutlinedTextField(
                        value = maxText,
                        onValueChange = { value ->
                            maxText = value
                            value.toIntOrNull()?.let { viewModel.setRange(minValue, it) }
                        },
                        label = { Text(stringResource(R.string.label_max)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.width(110.dp),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Generate button
                Button(
                    onClick = {
                        viewModel.dismissConfetti()
                        viewModel.generate()
                    },
                    enabled = !isAnimating && minValue <= maxValue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = if (isAnimating) stringResource(R.string.state_generating)
                        else stringResource(R.string.action_generate),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // History trail
                if (history.isNotEmpty()) {
                    Text(
                        text = history.take(8).joinToString("  \u2022  ") { it.label },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(0.3f))
            }

        }
    }

    ConfettiEffect(trigger = showConfetti)
}
