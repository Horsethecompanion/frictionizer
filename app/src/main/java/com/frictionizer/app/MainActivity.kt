package com.frictionizer.app

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.frictionizer.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false) // Use wordmark logo only

        ViewCompat.setOnApplyWindowInsetsListener(binding.mainRoot) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(
                top = systemBars.top + 48, // Further increase for status bar safety
                bottom = systemBars.bottom
            )
            insets
        }

        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        binding.btnEnableService.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
        binding.btnDisableBatteryOpt.setOnClickListener {
            startActivity(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:$packageName")
            })
        }
        binding.btnSelectApps.setOnClickListener {
            startActivity(Intent(this, AppSelectionActivity::class.java))
        }
        binding.btnManageActivities.setOnClickListener {
            startActivity(Intent(this, ActivitiesActivity::class.java))
        }
        binding.btnStats.setOnClickListener {
            startActivity(Intent(this, StatsActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        updateServiceStatus()
        updateBatteryStatus()
    }

    private fun updateServiceStatus() {
        val enabled = isAccessibilityServiceEnabled()
        if (enabled) {
            binding.tvServiceStatus.text = "Service active — Frictionizer is running"
            binding.cardStatus.setCardBackgroundColor(getColor(R.color.status_active))
            binding.btnEnableService.visibility = View.GONE
        } else {
            binding.tvServiceStatus.text = "Service inactive — tap to enable in Accessibility Settings"
            binding.cardStatus.setCardBackgroundColor(getColor(R.color.status_inactive))
            binding.btnEnableService.visibility = View.VISIBLE
        }
    }

    private fun updateBatteryStatus() {
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        binding.cardBattery.visibility =
            if (pm.isIgnoringBatteryOptimizations(packageName)) View.GONE else View.VISIBLE
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val service = "$packageName/${FrictionizerAccessibilityService::class.java.canonicalName}"
        val enabled = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
            ?: return false
        return enabled.split(":").any { it.equals(service, ignoreCase = true) }
    }
}
