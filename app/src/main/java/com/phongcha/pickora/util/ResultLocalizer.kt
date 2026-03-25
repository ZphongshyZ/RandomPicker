package com.phongcha.pickora.util

import android.content.Context
import com.phongcha.pickora.R

/**
 * Maps raw result IDs stored in history to localized display text.
 * Only needed for modes that store English IDs (yesno, coinflip).
 * Other modes store user-generated text which is already in the right language.
 */
object ResultLocalizer {
    fun localize(context: Context, result: String, pickerType: String): String {
        return when (pickerType) {
            "yesno" -> when (result) {
                "YES" -> context.getString(R.string.label_yes)
                "NO" -> context.getString(R.string.label_no)
                else -> result
            }
            "coinflip" -> when (result) {
                "Heads" -> context.getString(R.string.label_heads)
                "Tails" -> context.getString(R.string.label_tails)
                else -> result
            }
            else -> result
        }
    }
}
