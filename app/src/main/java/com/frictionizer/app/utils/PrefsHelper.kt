package com.frictionizer.app.utils

import android.content.Context
import org.json.JSONArray
import java.util.concurrent.TimeUnit

object PrefsHelper {
    private const val PREFS = "frictionizer_prefs"
    private const val KEY_MONITORED_APPS = "monitored_apps"
    private const val KEY_ACTIVITIES = "activities"
    private const val KEY_COUNTDOWN_START = "countdown_start_ms"
    private const val KEY_BEDTIME_ENABLED = "bedtime_enabled"
    private const val KEY_BEDTIME_HOUR = "bedtime_hour"
    private const val KEY_BEDTIME_MINUTE = "bedtime_minute"

    private val DEFAULT_ACTIVITIES = listOf(
        "Work / Research",
        "Exercise",
        "Filling time",
        "Doom scrolling",
        "Socialising"
    )

    // ── Monitored apps ───────────────────────────────────────────────────────

    fun getMonitoredApps(context: Context): Set<String> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return prefs.getStringSet(KEY_MONITORED_APPS, emptySet()) ?: emptySet()
    }

    fun setMonitoredApps(context: Context, apps: Set<String>) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putStringSet(KEY_MONITORED_APPS, apps).apply()
    }

    // ── Activities (max 5) ───────────────────────────────────────────────────

    fun getActivities(context: Context): MutableList<String> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_ACTIVITIES, null)
            ?: return DEFAULT_ACTIVITIES.toMutableList()
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { arr.getString(it) }.toMutableList()
        } catch (e: Exception) {
            DEFAULT_ACTIVITIES.toMutableList()
        }
    }

    fun setActivities(context: Context, activities: List<String>) {
        val capped = activities.take(5)
        val json = JSONArray(capped).toString()
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_ACTIVITIES, json).apply()
    }

    // ── Countdown / "boiling frog" ───────────────────────────────────────────

    /** Returns or lazily creates the countdown start date (ms since epoch). */
    fun getCountdownStartDate(context: Context): Long {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        var stored = prefs.getLong(KEY_COUNTDOWN_START, 0L)
        if (stored == 0L) {
            stored = System.currentTimeMillis()
            prefs.edit().putLong(KEY_COUNTDOWN_START, stored).apply()
        }
        return stored
    }

    /** Day 1 = 1 s, Day 2 = 2 s … capped at 60 s (day 60+). */
    fun getCountdownSeconds(context: Context): Int {
        val start = getCountdownStartDate(context)
        val days = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - start).toInt()
        return minOf(days + 1, 60)
    }

    /** Reset: start counting from today again. */
    fun resetCountdown(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putLong(KEY_COUNTDOWN_START, System.currentTimeMillis()).apply()
    }

    // ── Bedtime ──────────────────────────────────────────────────────────────

    fun isBedtimeEnabled(context: Context): Boolean {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_BEDTIME_ENABLED, true) // on by default
    }

    fun setBedtimeEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_BEDTIME_ENABLED, enabled).apply()
    }

    fun getBedtimeHour(context: Context): Int =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(KEY_BEDTIME_HOUR, 22)

    fun getBedtimeMinute(context: Context): Int =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(KEY_BEDTIME_MINUTE, 30)

    fun setBedtime(context: Context, hour: Int, minute: Int) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_BEDTIME_HOUR, hour)
            .putInt(KEY_BEDTIME_MINUTE, minute)
            .apply()
    }

    /**
     * Returns true if current time is within 2 hours before the set bedtime.
     * e.g. bedtime = 22:30 → window is 20:30–22:30
     */
    fun isInBedtimeWindow(context: Context): Boolean {
        if (!isBedtimeEnabled(context)) return false
        val cal = java.util.Calendar.getInstance()
        val nowMinutes = cal.get(java.util.Calendar.HOUR_OF_DAY) * 60 + cal.get(java.util.Calendar.MINUTE)
        val bedMinutes = getBedtimeHour(context) * 60 + getBedtimeMinute(context)
        val windowStart = bedMinutes - 120
        return if (windowStart >= 0) {
            nowMinutes in windowStart until bedMinutes
        } else {
            // Window wraps past midnight
            nowMinutes >= (windowStart + 1440) || nowMinutes < bedMinutes
        }
    }

    /** Returns the effective countdown seconds, applying bedtime minimum. */
    fun getEffectiveCountdownSeconds(context: Context): Int {
        val base = getCountdownSeconds(context)
        return if (isInBedtimeWindow(context)) maxOf(base, 15) else base
    }
}
