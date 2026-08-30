package com.frictionizer.app

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.frictionizer.app.data.AppDatabase
import com.frictionizer.app.data.Session
import com.frictionizer.app.utils.PrefsHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FrictionizerAccessibilityService : AccessibilityService() {

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private var audioManager: AudioManager? = null
    private var audioFocusRequest: AudioFocusRequest? = null

    private val sessionStartTimes = mutableMapOf<String, Long>()
    private val sessionActivities = mutableMapOf<String, String>()
    private val totalBackgroundTimes = mutableMapOf<String, Long>()
    private val lastExitTimes = mutableMapOf<String, Long>()
    private val unlockedUntil = mutableMapOf<String, Long>()
    private val recentlyDismissed = mutableSetOf<String>()
    private val handler = Handler(Looper.getMainLooper())
    private var lastForegroundPkg: String? = null
    private var countdownRunnable: Runnable? = null

    companion object {
        private const val GRACE_PERIOD_MS = 180_000L
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val pkg = event.packageName?.toString() ?: return
        val now = System.currentTimeMillis()

        if (pkg == packageName) return
        if (pkg == lastForegroundPkg) return

        val previousPkg = lastForegroundPkg
        lastForegroundPkg = pkg
        val monitored = PrefsHelper.getMonitoredApps(this)

        if (previousPkg != null && previousPkg in monitored) {
            lastExitTimes[previousPkg] = now
        }

        if (pkg in monitored) {
            val isUnlocked = now < (unlockedUntil[pkg] ?: 0L)
            if (isUnlocked) {
                val exitTime = lastExitTimes[pkg] ?: 0L
                if (exitTime > 0) {
                    val bgTime = now - exitTime
                    totalBackgroundTimes[pkg] = (totalBackgroundTimes[pkg] ?: 0L) + bgTime
                    lastExitTimes[pkg] = 0L
                }
                unlockedUntil[pkg] = now + GRACE_PERIOD_MS
            } else {
                if (sessionStartTimes.containsKey(pkg)) relock(pkg)
                if (pkg !in recentlyDismissed) showOverlay(pkg)
            }
        } else {
            // Switched to a non-monitored app - dismiss any active overlay
            dismissOverlay(null) // null pkg means don't mark as "recently dismissed"
        }

        val expired = lastExitTimes.filter { (p, exit) ->
            exit > 0 && now - exit > GRACE_PERIOD_MS && p != pkg
        }.keys
        expired.forEach { p -> relock(p) }
    }

    private fun showOverlay(pkg: String) {
        if (overlayView != null) return

        val appLabel = try {
            packageManager.getApplicationLabel(packageManager.getApplicationInfo(pkg, 0)).toString()
        } catch (e: Exception) { pkg }

        val activities = PrefsHelper.getActivities(this)
        val countdownSecs = PrefsHelper.getEffectiveCountdownSeconds(this)

        // Mute: grab audio focus so YouTube/etc pauses
        grabAudioFocus()

        val themedContext = ContextThemeWrapper(this, R.style.Theme_Frictionizer)
        val view = LayoutInflater.from(themedContext).inflate(R.layout.overlay_friction, null)

        view.findViewById<TextView>(R.id.tv_opening_app)?.text = "Opening $appLabel"

        val container = view.findViewById<LinearLayout>(R.id.activities_container)
        val hintText = view.findViewById<TextView>(R.id.tv_waiting_hint)
        val countdownText = view.findViewById<TextView>(R.id.tv_countdown)
        val progressBar = view.findViewById<ProgressBar>(R.id.countdown_progress)

        // Build activity rows (button-style, tap = immediate unlock)
        val activityButtons = mutableListOf<View>()
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).also { it.setMargins(0, 0, 0, 8) }

        activities.forEach { label ->
            val btn = com.google.android.material.button.MaterialButton(
                themedContext,
                null,
                com.google.android.material.R.attr.materialButtonOutlinedStyle
            ).apply {
                layoutParams = params
                text = label
                textSize = 16f
                isAllCaps = false
                setTextColor(Color.parseColor("#DCE5F0"))
                strokeColor = android.content.res.ColorStateList.valueOf(Color.parseColor("#2E3F55"))
                cornerRadius = 40 // Use local dimension or hardcoded safe value
                setBackgroundColor(Color.TRANSPARENT)
                isEnabled = false
                alpha = 0.4f
                setOnClickListener {
                    val now = System.currentTimeMillis()
                    sessionActivities[pkg] = label
                    sessionStartTimes[pkg] = now
                    unlockedUntil[pkg] = now + GRACE_PERIOD_MS
                    countdownRunnable?.let { handler.removeCallbacks(it) }
                    // releaseAudioFocus() // Moved to dismissOverlay for smooth transition
                    dismissOverlay(pkg)
                }
            }
            container?.addView(btn)
            activityButtons.add(btn)
        }

        // Countdown
        val totalMs = countdownSecs * 1000L
        val startTime = System.currentTimeMillis()

        var tickRunnable: Runnable? = null
        tickRunnable = object : Runnable {
            override fun run() {
                val elapsed = System.currentTimeMillis() - startTime
                val remaining = ((totalMs - elapsed) / 1000L).toInt().coerceAtLeast(0)
                val progress = ((totalMs - elapsed).toFloat() / totalMs * 100).toInt().coerceIn(0, 100)

                progressBar?.progress = progress
                countdownText?.text = if (remaining > 0) remaining.toString() else ""

                if (remaining > 0) {
                    handler.postDelayed(this, 200)
                } else {
                    // Unlock options
                    countdownText?.visibility = View.GONE
                    progressBar?.visibility = View.GONE
                    hintText?.visibility = View.GONE
                    activityButtons.forEach { btn ->
                        btn.isEnabled = true
                        btn.alpha = 1f
                    }
                }
            }
        }
        countdownRunnable = tickRunnable
        handler.post(tickRunnable)

        val wmParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or 
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, // Allow system nav to work
            PixelFormat.TRANSLUCENT
        ).also { it.gravity = Gravity.CENTER }

        try {
            windowManager?.addView(view, wmParams)
            overlayView = view
            // Entrance animation
            view.findViewById<View>(R.id.card_view)?.apply {
                alpha = 0f
                translationY = 60f
                animate().alpha(1f).translationY(0f).setDuration(350)
                    .setInterpolator(android.view.animation.DecelerateInterpolator()).start()
            }
        } catch (e: Exception) {
            Log.e("Frictionizer", "Could not show overlay", e)
        }
    }

    private fun dismissOverlay(pkg: String?) {
        countdownRunnable?.let { handler.removeCallbacks(it) }
        countdownRunnable = null
        
        val targetView = overlayView
        overlayView = null // Immediate clear to prevent multiple calls
        
        targetView?.animate()?.alpha(0f)?.setDuration(1000)?.withEndAction {
            targetView.let { try { windowManager?.removeView(it) } catch (e: Exception) { } }
            releaseAudioFocus()
        }?.start() ?: releaseAudioFocus() // Ensure focus is released even if view is missing
        
        if (pkg != null) {
            recentlyDismissed.add(pkg)
            handler.postDelayed({ recentlyDismissed.remove(pkg) }, 2000)
        }
    }

    // ── Audio focus ───────────────────────────────────────────────────────────

    @Suppress("DEPRECATION")
    private fun grabAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val req = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                .setOnAudioFocusChangeListener { }
                .build()
            audioManager?.requestAudioFocus(req)
            audioFocusRequest = req
        } else {
            audioManager?.requestAudioFocus(
                { }, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE
            )
        }
    }

    @Suppress("DEPRECATION")
    private fun releaseAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager?.abandonAudioFocusRequest(it) }
            audioFocusRequest = null
        } else {
            audioManager?.abandonAudioFocus(null)
        }
    }

    // ── Session tracking ──────────────────────────────────────────────────────

    private fun endSession(pkg: String) {
        val startTime = sessionStartTimes[pkg] ?: return
        val activity = sessionActivities[pkg] ?: return
        val exitTime = lastExitTimes[pkg] ?: 0L
        val endTime = if (exitTime > 0) exitTime else System.currentTimeMillis()
        val duration = (endTime - startTime) - (totalBackgroundTimes[pkg] ?: 0L)

        if (duration > 1000) {
            val appLabel = try {
                packageManager.getApplicationLabel(packageManager.getApplicationInfo(pkg, 0)).toString()
            } catch (e: Exception) { pkg }
            CoroutineScope(Dispatchers.IO).launch {
                AppDatabase.getDatabase(this@FrictionizerAccessibilityService)
                    .sessionDao().insert(Session(
                        packageName = pkg, appLabel = appLabel, activityName = activity,
                        startTime = startTime, durationMs = duration
                    ))
            }
        }
        sessionStartTimes.remove(pkg)
        sessionActivities.remove(pkg)
        totalBackgroundTimes.remove(pkg)
        lastExitTimes.remove(pkg)
    }

    private fun relock(pkg: String) {
        endSession(pkg)
        unlockedUntil.remove(pkg)
    }

    override fun onInterrupt() {
        releaseAudioFocus()
        overlayView?.let { try { windowManager?.removeView(it) } catch (e: Exception) {} }
        overlayView = null
        sessionStartTimes.keys.toList().forEach { endSession(it) }
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseAudioFocus()
        overlayView?.let { try { windowManager?.removeView(it) } catch (e: Exception) {} }
        overlayView = null
        sessionStartTimes.keys.toList().forEach { endSession(it) }
    }
}
