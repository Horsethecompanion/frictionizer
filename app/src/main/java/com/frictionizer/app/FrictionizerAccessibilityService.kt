package com.frictionizer.app

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.frictionizer.app.data.AppDatabase
import com.frictionizer.app.data.Session
import com.frictionizer.app.utils.PrefsHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FrictionizerAccessibilityService : AccessibilityService() {

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null

    // Per-package tracking
    private val sessionStartTimes = mutableMapOf<String, Long>()
    private val sessionActivities = mutableMapOf<String, String>()
    private val totalBackgroundTimes = mutableMapOf<String, Long>()
    private val lastExitTimes = mutableMapOf<String, Long>()
    private val unlockedUntil = mutableMapOf<String, Long>() // pkg -> timestamp

    // Prevents re-showing overlay immediately after dismissal (e.g. orientation)
    private val recentlyDismissed = mutableSetOf<String>()
    private val handler = Handler(Looper.getMainLooper())
    private var lastForegroundPkg: String? = null

    companion object {
        private const val GRACE_PERIOD_MS = 180_000L // 3 minutes
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val pkg = event.packageName?.toString() ?: return
        val now = System.currentTimeMillis()

        // Skip our own app
        if (pkg == packageName) return
        // Skip if same package (in-app navigation)
        if (pkg == lastForegroundPkg) return

        val previousPkg = lastForegroundPkg
        lastForegroundPkg = pkg

        val monitored = PrefsHelper.getMonitoredApps(this)

        // Handle leaving the previous app
        if (previousPkg != null && previousPkg in monitored) {
            lastExitTimes[previousPkg] = now
            Log.d("Frictionizer", "Leaving monitored app $previousPkg. Grace period starts.")
        }

        // Handle entering a monitored app
        if (pkg in monitored) {
            val unlockedTime = unlockedUntil[pkg] ?: 0L
            val isUnlocked = now < unlockedTime

            Log.d("Frictionizer", "Entering $pkg. Is Unlocked: $isUnlocked")

            if (isUnlocked) {
                // Resume session: calculate background time
                val exitTime = lastExitTimes[pkg] ?: 0L
                if (exitTime > 0) {
                    val bgTime = now - exitTime
                    totalBackgroundTimes[pkg] = (totalBackgroundTimes[pkg] ?: 0L) + bgTime
                    lastExitTimes[pkg] = 0L
                    Log.d("Frictionizer", "Resuming session for $pkg. Added ${bgTime/1000}s background time.")
                }
                // Extend unlock window while in foreground
                unlockedUntil[pkg] = now + GRACE_PERIOD_MS
            } else {
                // Session expired or never started
                if (sessionStartTimes.containsKey(pkg)) {
                    relock(pkg)
                }
                if (pkg !in recentlyDismissed) {
                    showOverlay(pkg)
                }
            }
        }

        // Cleanup: End background sessions that passed the grace period
        val expired = lastExitTimes.filter { (p, exit) ->
            exit > 0 && now - exit > GRACE_PERIOD_MS && p != pkg
        }.keys
        expired.forEach { p ->
            Log.d("Frictionizer", "Grace period expired in background for $p. Ending session and relocking.")
            relock(p)
        }
    }

    private fun showOverlay(pkg: String) {
        if (overlayView != null) return
        Log.d("Frictionizer", "Showing overlay for $pkg")

        val appLabel = try {
            packageManager.getApplicationLabel(packageManager.getApplicationInfo(pkg, 0)).toString()
        } catch (e: Exception) { pkg }

        val activities = PrefsHelper.getActivities(this)

        val themedContext = ContextThemeWrapper(this, R.style.Theme_Frictionizer)
        val inflater = LayoutInflater.from(themedContext)
        val view = inflater.inflate(R.layout.overlay_friction, null)

        // Populate app label
        view.findViewById<TextView>(R.id.tv_opening_app)?.text = "Opening $appLabel"

        // Build chips from activities list
        val chipGroup = view.findViewById<ChipGroup>(R.id.chip_group_activities)
        chipGroup?.removeAllViews()
        activities.forEach { label ->
            val chip = Chip(themedContext).apply {
                id = View.generateViewId()
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                text = label
                isCheckable = true
                setTextColor(Color.WHITE)
                setChipBackgroundColorResource(R.color.card_dark)
                chipStrokeColor = android.content.res.ColorStateList.valueOf(Color.parseColor("#2C2C3A"))
                chipStrokeWidth = 2f
                rippleColor = android.content.res.ColorStateList.valueOf(Color.parseColor("#3300E5FF"))
                textAlignment = View.TEXT_ALIGNMENT_CENTER
            }
            chipGroup?.addView(chip)
        }

        // Go button
        val btnGo = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_go)
        btnGo?.isEnabled = false
        btnGo?.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#1A00E5FF")) // Very dim teal

        chipGroup?.setOnCheckedStateChangeListener { _, checkedIds ->
            val isChecked = checkedIds.isNotEmpty()
            btnGo?.isEnabled = isChecked
            if (isChecked) {
                btnGo?.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#00E5FF")) // Bright teal
            } else {
                btnGo?.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#1A00E5FF"))
            }
        }

        btnGo?.setOnClickListener {
            val checkedId = chipGroup?.checkedChipId ?: -1
            val chosenActivity = if (checkedId != View.NO_ID) {
                val chip = view.findViewById<Chip>(checkedId)
                chip?.text?.toString() ?: "Unspecified"
            } else {
                "Unspecified"
            }
            
            val now = System.currentTimeMillis()
            sessionActivities[pkg] = chosenActivity
            sessionStartTimes[pkg] = now
            unlockedUntil[pkg] = now + GRACE_PERIOD_MS
            
            dismissOverlay(pkg)
        }

        // Skip button — no session tracking
        val btnSkip = view.findViewById<Button>(R.id.btn_skip)
        btnSkip?.visibility = View.INVISIBLE
        handler.postDelayed({
            btnSkip?.visibility = View.VISIBLE
        }, 2000)

        btnSkip?.setOnClickListener {
            dismissOverlay(pkg)
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.CENTER

        try {
            windowManager?.addView(view, params)
            overlayView = view

            // Simple entrance animation
            val cardView = view.findViewById<View>(R.id.card_view)
            cardView?.alpha = 0f
            cardView?.translationY = 50f
            cardView?.animate()
                ?.alpha(1f)
                ?.translationY(0f)
                ?.setDuration(400)
                ?.setInterpolator(android.view.animation.DecelerateInterpolator())
                ?.start()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun dismissOverlay(pkg: String) {
        Log.d("Frictionizer", "Dismissing overlay for $pkg")
        overlayView?.let {
            try { windowManager?.removeView(it) } catch (e: Exception) { e.printStackTrace() }
        }
        overlayView = null

        // Prevent immediately re-triggering for a short window
        recentlyDismissed.add(pkg)
        handler.postDelayed({ recentlyDismissed.remove(pkg) }, 2000)
    }

    private fun endSession(pkg: String) {
        val startTime = sessionStartTimes[pkg] ?: return
        val activity = sessionActivities[pkg] ?: return
        
        val exitTime = lastExitTimes[pkg] ?: 0L
        val endTime = if (exitTime > 0) exitTime else System.currentTimeMillis()
        
        val rawDuration = endTime - startTime
        val bgTime = totalBackgroundTimes[pkg] ?: 0L
        val duration = rawDuration - bgTime

        if (duration > 1000) { // ignore sub-second blips
            val appLabel = try {
                packageManager.getApplicationLabel(packageManager.getApplicationInfo(pkg, 0)).toString()
            } catch (e: Exception) { pkg }

            val session = Session(
                packageName = pkg,
                appLabel = appLabel,
                activityName = activity,
                startTime = startTime,
                durationMs = duration
            )

            CoroutineScope(Dispatchers.IO).launch {
                AppDatabase.getDatabase(this@FrictionizerAccessibilityService)
                    .sessionDao().insert(session)
            }
        }

        // Cleanup state for this package
        sessionStartTimes.remove(pkg)
        sessionActivities.remove(pkg)
        totalBackgroundTimes.remove(pkg)
        lastExitTimes.remove(pkg)
        // unlockedUntil is intentionally NOT removed here, so it survives session ends (swipes)
    }

    private fun relock(pkg: String) {
        endSession(pkg)
        unlockedUntil.remove(pkg)
    }

    override fun onInterrupt() {
        overlayView?.let { try { windowManager?.removeView(it) } catch (e: Exception) {} }
        overlayView = null
        // End all active sessions
        val active = sessionStartTimes.keys.toList()
        active.forEach { endSession(it) }
    }

    override fun onDestroy() {
        super.onDestroy()
        overlayView?.let { try { windowManager?.removeView(it) } catch (e: Exception) {} }
        overlayView = null
        val active = sessionStartTimes.keys.toList()
        active.forEach { endSession(it) }
    }
}
