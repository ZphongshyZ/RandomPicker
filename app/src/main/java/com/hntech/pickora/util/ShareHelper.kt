package com.hntech.pickora.util

import android.content.Context
import android.content.Intent
import com.hntech.pickora.R

object ShareHelper {

    /**
     * Share a localized result. [displayResult] should already be the
     * user-facing localized text (e.g. from stringResource), not the raw
     * ViewModel ID.
     */
    fun shareResult(context: Context, displayResult: String, pickerType: String) {
        val message = when (pickerType) {
            "wheel" -> context.getString(R.string.share_wheel, displayResult)
            "number" -> context.getString(R.string.share_number, displayResult)
            "name" -> context.getString(R.string.share_name, displayResult)
            "yesno" -> context.getString(R.string.share_yesno, displayResult)
            "coinflip" -> context.getString(R.string.share_coinflip, displayResult)
            "race" -> context.getString(R.string.share_race, displayResult)
            else -> context.getString(R.string.share_default, displayResult)
        }
        val text = "\uD83C\uDF89 $message${context.getString(R.string.share_suffix)}"

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_result)))
    }
}
