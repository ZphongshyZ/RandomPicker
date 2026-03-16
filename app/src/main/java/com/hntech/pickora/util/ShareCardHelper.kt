package com.hntech.pickora.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import androidx.core.content.FileProvider
import java.io.File

/**
 * Creates a branded share card image and shares it via intent.
 * This gives viral-ready output instead of plain text.
 */
object ShareCardHelper {

    fun shareAsImage(
        context: Context,
        result: String,
        subtitle: String,
        modeEmoji: String,
        primaryColor: Int = Color.parseColor("#FF6B6B"),
        secondaryColor: Int = Color.parseColor("#FFB347")
    ) {
        val bitmap = createCard(result, subtitle, modeEmoji, primaryColor, secondaryColor)
        val file = File(context.cacheDir, "share_card.png")
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 95, it) }

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, "\uD83C\uDF89 $result — Pickora App")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, context.getString(com.hntech.pickora.R.string.share_result)))
    }

    private fun createCard(
        result: String,
        subtitle: String,
        modeEmoji: String,
        primaryColor: Int,
        secondaryColor: Int
    ): Bitmap {
        val width = 1080
        val height = 600
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background gradient
        val bgPaint = Paint().apply {
            shader = LinearGradient(0f, 0f, width.toFloat(), height.toFloat(), primaryColor, secondaryColor, Shader.TileMode.CLAMP)
        }
        canvas.drawRoundRect(RectF(0f, 0f, width.toFloat(), height.toFloat()), 48f, 48f, bgPaint)

        // Dark overlay for text readability
        val overlayPaint = Paint().apply { color = Color.argb(80, 0, 0, 0) }
        canvas.drawRoundRect(RectF(0f, 0f, width.toFloat(), height.toFloat()), 48f, 48f, overlayPaint)

        // Emoji
        val emojiPaint = Paint().apply {
            textSize = 120f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(modeEmoji, width / 2f, 160f, emojiPaint)

        // Result text
        val resultPaint = Paint().apply {
            color = Color.WHITE
            textSize = if (result.length > 15) 72f else 96f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
            setShadowLayer(4f, 2f, 2f, Color.argb(100, 0, 0, 0))
        }
        canvas.drawText(result, width / 2f, 320f, resultPaint)

        // Subtitle
        val subPaint = Paint().apply {
            color = Color.argb(200, 255, 255, 255)
            textSize = 40f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(subtitle, width / 2f, 400f, subPaint)

        // App branding
        val brandPaint = Paint().apply {
            color = Color.argb(150, 255, 255, 255)
            textSize = 32f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("Pickora", width / 2f, 540f, brandPaint)

        return bitmap
    }
}
