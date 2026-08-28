package com.frictionizer.app

import android.app.TimePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.frictionizer.app.databinding.ActivitySettingsBinding
import com.frictionizer.app.utils.PrefsHelper

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var resetCode: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            v.updatePadding(top = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top)
            insets
        }

        setupCountdownSection()
        setupBedtimeSection()
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }

    // ── Countdown section ─────────────────────────────────────────────────────

    private fun setupCountdownSection() {
        val secs = PrefsHelper.getEffectiveCountdownSeconds(this)
        val base = PrefsHelper.getCountdownSeconds(this)
        val dayMsg = when {
            base >= 60 -> "Day 60+ — you've reached the 60 second cap."
            else -> {
                val days = base - 1
                "Day ${days + 1} — wait is ${secs}s${if (PrefsHelper.isInBedtimeWindow(this)) " (bedtime min applies)" else ""}."
            }
        }
        binding.tvCountdownInfo.text = dayMsg

        // Generate 10 random capital letters
        resetCode = generateResetCode()
        binding.tvResetCode.text = resetCode

        binding.etResetInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val match = s?.toString().equals(resetCode, ignoreCase = false)
                binding.btnConfirmReset.isEnabled = match
                binding.btnConfirmReset.alpha = if (match) 1f else 0.4f
            }
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
        })

        binding.btnConfirmReset.alpha = 0.4f
        binding.btnConfirmReset.setOnClickListener {
            PrefsHelper.resetCountdown(this)
            binding.tvCountdownInfo.text = "Reset! Wait is back to 1 second."
            binding.etResetInput.setText("")
            // Regenerate code so they can't reuse it
            resetCode = generateResetCode()
            binding.tvResetCode.text = resetCode
            Toast.makeText(this, "Countdown reset to day 1", Toast.LENGTH_SHORT).show()
        }
    }

    private fun generateResetCode(): String {
        val letters = ('A'..'Z').toList()
        return (1..10).map { letters.random() }.joinToString("")
    }

    // ── Bedtime section ───────────────────────────────────────────────────────

    private fun setupBedtimeSection() {
        binding.switchBedtime.isChecked = PrefsHelper.isBedtimeEnabled(this)
        updateBedtimeTimeButton()

        binding.switchBedtime.setOnCheckedChangeListener { _, checked ->
            PrefsHelper.setBedtimeEnabled(this, checked)
            binding.rowBedtimeTime.alpha = if (checked) 1f else 0.4f
        }
        binding.rowBedtimeTime.alpha = if (PrefsHelper.isBedtimeEnabled(this)) 1f else 0.4f

        binding.btnPickBedtime.setOnClickListener {
            val h = PrefsHelper.getBedtimeHour(this)
            val m = PrefsHelper.getBedtimeMinute(this)
            TimePickerDialog(this, { _, hour, minute ->
                PrefsHelper.setBedtime(this, hour, minute)
                updateBedtimeTimeButton()
            }, h, m, false).show()
        }
    }

    private fun updateBedtimeTimeButton() {
        val h = PrefsHelper.getBedtimeHour(this)
        val m = PrefsHelper.getBedtimeMinute(this)
        val amPm = if (h >= 12) "PM" else "AM"
        val displayH = when { h == 0 -> 12; h > 12 -> h - 12; else -> h }
        binding.btnPickBedtime.text = "%d:%02d %s".format(displayH, m, amPm)
    }
}
