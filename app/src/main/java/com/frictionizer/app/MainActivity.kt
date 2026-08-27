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

        // Handle system insets for edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(binding.mainScrollView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(
                left = systemBars.left,
                top = systemBars.top,
                right = systemBars.right,
                bottom = systemBars.bottom
            )
            insets
        }

        binding.btnEnableService.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        binding.btnDisableBatteryOpt.setOnClickListener {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:$packageName")
            }
            startActivity(intent)
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
            binding.tvServiceStatus.text = "Service Active — Frictionizer is running"
            binding.cardStatus.setCardBackgroundColor(getColor(R.color.status_active))
            binding.btnEnableService.visibility = View.GONE
        } else {
            binding.tvServiceStatus.text = "Service Inactive — Tap below to enable in Accessibility Settings"
            binding.cardStatus.setCardBackgroundColor(getColor(R.color.status_inactive))
            binding.btnEnableService.visibility = View.VISIBLE
        }
    }

    private fun updateBatteryStatus() {
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        val isIgnoring = pm.isIgnoringBatteryOptimizations(packageName)
        binding.cardBattery.visibility = if (isIgnoring) View.GONE else View.VISIBLE
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val service = "$packageName/${FrictionizerAccessibilityService::class.java.canonicalName}"
        val enabledServices = Settings.Secure.getString(
            contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return enabledServices.split(":").any {
            it.equals(service, ignoreCase = true)
        }
    }
}
