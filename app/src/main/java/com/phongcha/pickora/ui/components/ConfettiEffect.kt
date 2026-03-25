package com.phongcha.pickora.ui.components

import android.view.WindowManager
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private val confettiColors = listOf(
    Color(0xFFE53935), Color(0xFF43A047), Color(0xFF1E88E5),
    Color(0xFFFDD835), Color(0xFF8E24AA), Color(0xFFFF6F00),
    Color(0xFFE91E63), Color(0xFF00ACC1), Color(0xFF7CB342),
    Color(0xFFFF7043), Color(0xFF5C6BC0), Color(0xFFFFB300)
)

private enum class ParticleShape { RECT, CIRCLE, STRIP, DIAMOND }

private data class ConfettiParticle(
    val side: Int,
    val velocityX: Float,
    val velocityY: Float,
    val color: Color,
    val size: Float,
    val shape: ParticleShape,
    val rotationSpeed: Float,
    val initialRotation: Float,
    val gravity: Float,
    val drag: Float,
    val wobbleFreq: Float,
    val wobbleAmp: Float
)

private fun createParticles(): List<ConfettiParticle> {
    val shapes = ParticleShape.entries
    return List(100) { i ->
        val side = if (i < 50) 0 else 1
        val baseAngle = if (side == 0) {
            -30.0 - Random.nextDouble() * 60.0
        } else {
            -90.0 - Random.nextDouble() * 60.0
        }
        val angleRad = Math.toRadians(baseAngle)
        val power = 600f + Random.nextFloat() * 800f

        ConfettiParticle(
            side = side,
            velocityX = (cos(angleRad) * power).toFloat(),
            velocityY = (sin(angleRad) * power).toFloat(),
            color = confettiColors[Random.nextInt(confettiColors.size)],
            size = 8f + Random.nextFloat() * 12f,
            shape = shapes[Random.nextInt(shapes.size)],
            rotationSpeed = 200f + Random.nextFloat() * 600f,
            initialRotation = Random.nextFloat() * 360f,
            gravity = 800f + Random.nextFloat() * 400f,
            drag = 0.96f + Random.nextFloat() * 0.03f,
            wobbleFreq = 3f + Random.nextFloat() * 5f,
            wobbleAmp = 15f + Random.nextFloat() * 25f
        )
    }
}

/**
 * Fullscreen confetti cannon effect.
 * Renders as a transparent Dialog window — on top of ALL other dialogs.
 * Touch events pass through so users can interact with content underneath.
 * Once triggered, animation plays to completion even if [trigger] becomes false.
 */
@Composable
fun ConfettiEffect(
    trigger: Boolean,
    modifier: Modifier = Modifier
) {
    var isPlaying by remember { mutableStateOf(false) }
    val progress = remember { Animatable(0f) }
    val particles = remember { mutableStateOf(createParticles()) }

    LaunchedEffect(trigger) {
        if (trigger && !isPlaying) {
            isPlaying = true
            particles.value = createParticles()
            progress.snapTo(0f)
            try {
                progress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 3000, easing = LinearEasing)
                )
            } finally {
                isPlaying = false
            }
        }
    }

    if (!isPlaying) return

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        // Make dialog transparent + touch-passthrough
        (LocalView.current.parent as? DialogWindowProvider)?.window?.let { window ->
            SideEffect {
                window.setDimAmount(0f)
                window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                window.addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
                window.setBackgroundDrawableResource(android.R.color.transparent)
            }
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val t = progress.value
            val time = t * 3f

            particles.value.forEach { p ->
                val originX = if (p.side == 0) w * 0.1f else w * 0.9f
                val originY = h * 0.65f

                var vx = p.velocityX
                var vy = p.velocityY
                var px = originX
                var py = originY

                val steps = (time * 60).toInt()
                val dt = 1f / 60f
                for (step in 0 until steps) {
                    vy += p.gravity * dt
                    vx *= p.drag
                    vy *= p.drag
                    px += vx * dt
                    py += vy * dt
                }

                val wobble = sin(time * p.wobbleFreq.toDouble()).toFloat() * p.wobbleAmp * t
                px += wobble

                if (py > h + 50f || px < -50f || px > w + 50f) return@forEach

                val alpha = when {
                    t > 0.7f -> ((1f - t) / 0.3f).coerceIn(0f, 1f)
                    t < 0.05f -> (t / 0.05f).coerceIn(0f, 1f)
                    else -> 1f
                }
                if (alpha <= 0f) return@forEach

                val rotation = p.initialRotation + time * p.rotationSpeed
                val sz = p.size

                rotate(degrees = rotation, pivot = Offset(px, py)) {
                    when (p.shape) {
                        ParticleShape.RECT -> drawRect(
                            color = p.color.copy(alpha = alpha),
                            topLeft = Offset(px - sz / 2, py - sz * 0.3f),
                            size = Size(sz, sz * 0.6f)
                        )
                        ParticleShape.CIRCLE -> drawCircle(
                            color = p.color.copy(alpha = alpha),
                            radius = sz * 0.4f,
                            center = Offset(px, py)
                        )
                        ParticleShape.STRIP -> drawRect(
                            color = p.color.copy(alpha = alpha),
                            topLeft = Offset(px - sz * 0.15f, py - sz * 0.6f),
                            size = Size(sz * 0.3f, sz * 1.2f)
                        )
                        ParticleShape.DIAMOND -> {
                            val path = Path().apply {
                                moveTo(px, py - sz * 0.4f)
                                lineTo(px + sz * 0.3f, py)
                                lineTo(px, py + sz * 0.4f)
                                lineTo(px - sz * 0.3f, py)
                                close()
                            }
                            drawPath(path, color = p.color.copy(alpha = alpha))
                        }
                    }
                }
            }
        }
    }
}
