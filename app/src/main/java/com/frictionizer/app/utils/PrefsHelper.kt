package com.frictionizer.app.utils

import android.content.Context
import org.json.JSONArray

object PrefsHelper {
    private const val PREFS = "frictionizer_prefs"
    private const val KEY_MONITORED_APPS = "monitored_apps"
    private const val KEY_ACTIVITIES = "activities"

    private val DEFAULT_ACTIVITIES = listOf("Research", "Work", "On treadmill", "Bored", "Waiting", "Relaxing")

    fun getMonitoredApps(context: Context): Set<String> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return prefs.getStringSet(KEY_MONITORED_APPS, emptySet()) ?: emptySet()
    }

    fun setMonitoredApps(context: Context, apps: Set<String>) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putStringSet(KEY_MONITORED_APPS, apps).apply()
    }

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
        val json = JSONArray(activities).toString()
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_ACTIVITIES, json).apply()
    }
}
