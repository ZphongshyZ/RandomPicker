package com.phongcha.pickora.ui.coinflip

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.phongcha.pickora.R
import com.phongcha.pickora.ui.components.ConfettiEffect
import com.phongcha.pickora.util.FeedbackManager
import com.phongcha.pickora.util.ShareHelper
import org.koin.androidx.compose.koinViewModel
import kotlin.math.abs
import kotlin.math.cos

// Casino palette
private val GoldFace = Color(0xFFFFD700)
private val GoldRim = Color(0xFFDAA520)
private val GoldText = Color(0xFF8B6508)
private val SilverFace = Color(0xFFE0E0E0)
private val SilverRim = Color(0xFFBDBDBD)
private val SilverText = Color(0xFF424242)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoinFlipScreen(
    onBack: () -> Unit,
    viewModel: CoinFlipViewModel = koinViewModel()
) {
    val isAnimating by viewModel.isAnimating.collectAsState()
    val result by viewModel.result.collectAsState()
    val targetFlipRotation by viewModel.targetFlipRotation.collectAsState()
    val showConfetti by viewModel.showConfetti.collectAsState()
    val context = LocalContext.current
    val feedbackManager: FeedbackManager = remember { org.koin.java.KoinJavaComponent.getKoin().get() }

    val rotation = remember { Animatable(0f) }

    LaunchedEffect(targetFlipRotation) {
        if (targetFlipRotation != 0f && isAnimating) {
            feedbackManager.impact()
            rotation.animateTo(
                targetValue = targetFlipRotation,
                animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing)
            )
            feedbackManager.celebrate()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.coinflip_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                actions = {
                    result?.let {
                        IconButton(onClick = {
                            val localizedResult = if (it.id == "heads") context.getString(R.string.label_heads) else context.getString(R.string.label_tails)
                            ShareHelper.shareResult(context, localizedResult, "coinflip")
                        }) {
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
                Spacer(modifier = Modifier.weight(1f))

                // The coin — casino style with rim, shadow, embossed letter
                val flipAngle = rotation.value % 360f
                val cosAngle = cos(Math.toRadians(flipAngle.toDouble())).toFloat()
                val showingHeads = cosAngle >= 0

                Canvas(modifier = Modifier.size(220.dp)) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val radius = size.minDimension / 2f
                    val scaleX = abs(cosAngle).coerceAtLeast(0.05f)

                    scale(scaleX = scaleX, scaleY = 1f, pivot = center) {
                        // Shadow ring
                        drawCircle(
                            color = Color.Black.copy(alpha = 0.1f),
                            radius = radius + 4f,
                            center = Offset(center.x + 3f, center.y + 4f)
                        )
                        // Outer rim
                        drawCircle(
                            color = if (showingHeads) GoldRim else SilverRim,
                            radius = radius,
                            center = center
                        )
                        // Face
                        drawCircle(
                            color = if (showingHeads) GoldFace else SilverFace,
                            radius = radius * 0.92f,
                            center = center
                        )
                        // Inner decorative ring
                        drawCircle(
                            color = if (showingHeads) GoldRim.copy(alpha = 0.4f) else SilverRim.copy(alpha = 0.5f),
                            radius = radius * 0.78f,
                            center = center,
                            style = Stroke(width = 2f)
                        )

                        // Embossed letter
                        drawContext.canvas.nativeCanvas.apply {
                            val paint = android.graphics.Paint().apply {
                                color = if (showingHeads)
                                    android.graphics.Color.rgb(139, 101, 8)
                                else
                                    android.graphics.Color.rgb(66, 66, 66)
                                textSize = radius * 0.5f
                                textAlign = android.graphics.Paint.Align.CENTER
                                isFakeBoldText = true
                                isAntiAlias = true
                                setShadowLayer(2f, 1f, 1f,
                                    if (showingHeads) android.graphics.Color.argb(60, 139, 101, 8)
                                    else android.graphics.Color.argb(60, 0, 0, 0)
                                )
                            }
                            drawText(
                                if (showingHeads) "H" else "T",
                                center.x,
                                center.y + paint.textSize / 3f,
                                paint
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Result label
                result?.let {
                    val label = if (it.id == "heads") stringResource(R.string.label_heads)
                    else stringResource(R.string.label_tails)
                    Text(
                        text = label,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        letterSpacing = 2.sp
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Flip button
                Button(
                    onClick = {
                        viewModel.dismissConfetti()
                        viewModel.flip()
                    },
                    enabled = !isAnimating,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = if (isAnimating) stringResource(R.string.state_flipping)
                        else stringResource(R.string.action_flip),
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
