package com.phongcha.pickora.util

import android.content.Context
import android.content.SharedPreferences

/**
 * Tracks usage and prompts for review after positive engagement.
 * Shows a prompt after every 5th pick, but only once per session,
 * and respects "later" dismissal for 3 sessions.
 */
object ReviewHelper {

    private const val PREF_NAME = "review_prefs"
    private const val KEY_PICK_COUNT = "pick_count"
    private const val KEY_SESSIONS_SINCE_DISMISS = "sessions_since_dismiss"
    private const val KEY_HAS_REVIEWED = "has_reviewed"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun recordPick(context: Context) {
        val p = prefs(context)
        p.edit().putInt(KEY_PICK_COUNT, p.getInt(KEY_PICK_COUNT, 0) + 1).apply()
    }

    fun shouldShowPrompt(context: Context): Boolean {
        val p = prefs(context)
        if (p.getBoolean(KEY_HAS_REVIEWED, false)) return false
        val pickCount = p.getInt(KEY_PICK_COUNT, 0)
        val sessionsSinceDismiss = p.getInt(KEY_SESSIONS_SINCE_DISMISS, 3)
        return pickCount >= 5 && pickCount % 5 == 0 && sessionsSinceDismiss >= 3
    }

    fun onDismissed(context: Context) {
        prefs(context).edit().putInt(KEY_SESSIONS_SINCE_DISMISS, 0).apply()
    }

    fun onReviewed(context: Context) {
        prefs(context).edit().putBoolean(KEY_HAS_REVIEWED, true).apply()
    }

    fun incrementSession(context: Context) {
        val p = prefs(context)
        p.edit().putInt(KEY_SESSIONS_SINCE_DISMISS, p.getInt(KEY_SESSIONS_SINCE_DISMISS, 0) + 1).apply()
    }
}
