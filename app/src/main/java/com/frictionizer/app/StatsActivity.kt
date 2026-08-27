package com.frictionizer.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.frictionizer.app.data.AppDatabase
import com.frictionizer.app.databinding.ActivityStatsBinding
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.*

class StatsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStatsBinding
    private val db by lazy { AppDatabase.getDatabase(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityStatsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Handle insets
        ViewCompat.setOnApplyWindowInsetsListener(binding.statsRoot) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(
                left = systemBars.left,
                top = systemBars.top,
                right = systemBars.right,
                bottom = systemBars.bottom
            )
            insets
        }

        binding.recyclerStats.layoutManager = LinearLayoutManager(this)

        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("By App"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("By Activity"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Detailed"))

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) = loadData(tab.position)
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        loadData(0)

        binding.btnClearData.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Clear all data?")
                .setMessage("This will delete all recorded sessions permanently.")
                .setPositiveButton("Clear") { _, _ ->
                    CoroutineScope(Dispatchers.IO).launch {
                        db.sessionDao().deleteAll()
                        withContext(Dispatchers.Main) { loadData(binding.tabLayout.selectedTabPosition) }
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun loadData(tab: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            val rows = withContext(Dispatchers.IO) {
                when (tab) {
                    0 -> db.sessionDao().getTotalByApp().map {
                        StatRow(it.appLabel, null, it.totalMs)
                    }
                    1 -> db.sessionDao().getTotalByActivity().map {
                        StatRow(it.activityName, null, it.totalMs)
                    }
                    else -> db.sessionDao().getDetailedBreakdown().map {
                        StatRow(it.appLabel, it.activityName, it.totalMs)
                    }
                }
            }
            if (rows.isEmpty()) {
                binding.tvNoData.visibility = View.VISIBLE
                binding.recyclerStats.visibility = View.GONE
            } else {
                binding.tvNoData.visibility = View.GONE
                binding.recyclerStats.visibility = View.VISIBLE
                binding.recyclerStats.adapter = StatAdapter(rows)
            }
        }
    }

    data class StatRow(val primary: String, val secondary: String?, val durationMs: Long)

    class StatAdapter(private val items: List<StatRow>) :
        RecyclerView.Adapter<StatAdapter.VH>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
            LayoutInflater.from(parent.context).inflate(R.layout.item_stat, parent, false)
        )
        override fun getItemCount() = items.size
        override fun onBindViewHolder(holder: VH, position: Int) {
            val row = items[position]
            holder.primary.text = row.primary
            holder.secondary.text = row.secondary ?: ""
            holder.secondary.visibility = if (row.secondary != null) View.VISIBLE else View.GONE
            holder.duration.text = formatDuration(row.durationMs)
        }

        private fun formatDuration(ms: Long): String {
            val totalSecs = ms / 1000
            val hours = totalSecs / 3600
            val mins = (totalSecs % 3600) / 60
            val secs = totalSecs % 60
            return when {
                hours > 0 -> "%dh %dm".format(hours, mins)
                mins > 0 -> "%dm %ds".format(mins, secs)
                else -> "%ds".format(secs)
            }
        }

        class VH(view: View) : RecyclerView.ViewHolder(view) {
            val primary: TextView = view.findViewById(R.id.tv_stat_primary)
            val secondary: TextView = view.findViewById(R.id.tv_stat_secondary)
            val duration: TextView = view.findViewById(R.id.tv_stat_duration)
        }
    }
}
