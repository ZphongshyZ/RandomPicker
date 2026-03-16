package com.hntech.pickora.ui.yesno

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hntech.pickora.R
import com.hntech.pickora.ui.components.ConfettiEffect
import com.hntech.pickora.util.ShareHelper
import org.koin.androidx.compose.koinViewModel

private val YesGreen = Color(0xFF43A047)
private val NoBold = Color(0xFFE53935)
private val YesBg = Color(0xFFE8F5E9)
private val NoBg = Color(0xFFFFEBEE)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YesNoScreen(
    onBack: () -> Unit,
    viewModel: YesNoViewModel = koinViewModel()
) {
    val displayAnswer by viewModel.displayAnswer.collectAsState()
    val isAnimating by viewModel.isAnimating.collectAsState()
    val result by viewModel.result.collectAsState()
    val showConfetti by viewModel.showConfetti.collectAsState()
    val context = LocalContext.current

    val isYes = displayAnswer == true
    val isNo = displayAnswer == false

    val displayText = when (displayAnswer) {
        true -> stringResource(R.string.label_yes)
        false -> stringResource(R.string.label_no)
        null -> stringResource(R.string.yesno_placeholder)
    }

    val textColor by animateColorAsState(
        targetValue = when {
            isYes -> YesGreen
            isNo -> NoBold
            else -> MaterialTheme.colorScheme.onSurface
        },
        animationSpec = tween(200),
        label = "yn_color"
    )
    val bgColor by animateColorAsState(
        targetValue = when {
            isYes && !isAnimating -> YesBg
            isNo && !isAnimating -> NoBg
            else -> Color.Transparent
        },
        animationSpec = tween(400),
        label = "yn_bg"
    )
    val textScale by animateFloatAsState(
        targetValue = if (isAnimating) 0.9f else 1f,
        animationSpec = tween(100),
        label = "yn_scale"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.yesno_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                actions = {
                    result?.let {
                        IconButton(onClick = {
                            val localizedResult = if (it.id == "yes") context.getString(R.string.label_yes) else context.getString(R.string.label_no)
                            ShareHelper.shareResult(context, localizedResult, "yesno")
                        }) {
                            Icon(Icons.Default.Share, contentDescription = stringResource(R.string.action_share))
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Full-screen tinted background when result is shown
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(bgColor)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.weight(1f))

                // The answer — massive, bold, decisive, auto-sized
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Scale font based on available width and text length
                    // Each character ~0.65x font size in width (bold caps)
                    val charWidthRatio = 0.75f
                    val availableWidth = maxWidth.value
                    val fittedSize = availableWidth / (displayText.length * charWidthRatio)
                    val scaledSize = fittedSize.coerceIn(48f, 120f)
                    Text(
                        text = displayText,
                        fontSize = scaledSize.sp,
                        fontWeight = FontWeight.Black,
                        color = textColor,
                        textAlign = TextAlign.Center,
                        letterSpacing = 4.sp,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.scale(textScale)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Decide button
                Button(
                    onClick = {
                        viewModel.dismissConfetti()
                        viewModel.decide()
                    },
                    enabled = !isAnimating,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = if (isAnimating) stringResource(R.string.state_deciding)
                        else stringResource(R.string.action_decide),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

        }
    }

    ConfettiEffect(trigger = showConfetti)
}
