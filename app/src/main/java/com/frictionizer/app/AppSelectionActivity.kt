package com.frictionizer.app

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.frictionizer.app.databinding.ActivityAppSelectionBinding
import com.frictionizer.app.utils.PrefsHelper
import kotlinx.coroutines.*

class AppSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAppSelectionBinding
    private lateinit var adapter: AppAdapter
    private val selectedApps = mutableSetOf<String>()
    private var allApps: List<AppInfo> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAppSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Handle insets
        ViewCompat.setOnApplyWindowInsetsListener(binding.appSelectionRoot) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(
                left = systemBars.left,
                top = systemBars.top,
                right = systemBars.right,
                bottom = systemBars.bottom
            )
            insets
        }

        selectedApps.addAll(PrefsHelper.getMonitoredApps(this))
        adapter = AppAdapter(selectedApps) { pkg, checked ->
            if (checked) selectedApps.add(pkg) else selectedApps.remove(pkg)
            PrefsHelper.setMonitoredApps(this, selectedApps)
        }

        binding.recyclerApps.layoutManager = LinearLayoutManager(this)
        binding.recyclerApps.adapter = adapter

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                filterApps(newText)
                return true
            }
        })

        // Load apps in background
        CoroutineScope(Dispatchers.Main).launch {
            allApps = withContext(Dispatchers.IO) { loadUserApps() }
            adapter.setItems(allApps)
        }
    }

    private fun filterApps(query: String?) {
        val filtered = if (query.isNullOrBlank()) {
            allApps
        } else {
            allApps.filter { it.label.contains(query, ignoreCase = true) }
        }
        adapter.setItems(filtered)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun loadUserApps(): List<AppInfo> {
        val pm = packageManager
        val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        return installedApps
            .filter { app ->
                app.packageName != packageName &&
                        pm.getLaunchIntentForPackage(app.packageName) != null
            }
            .map { app ->
                AppInfo(
                    packageName = app.packageName,
                    label = app.loadLabel(pm).toString(),
                    icon = app.loadIcon(pm)
                )
            }
            .sortedBy { it.label }
    }

    data class AppInfo(val packageName: String, val label: String, val icon: Drawable)

    class AppAdapter(
        private val selectedPkgs: Set<String>,
        private val onToggle: (String, Boolean) -> Unit
    ) : RecyclerView.Adapter<AppAdapter.VH>() {

        private var items: List<AppInfo> = emptyList()

        fun setItems(list: List<AppInfo>) {
            items = list
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
            LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false)
        )

        override fun getItemCount() = items.size

        override fun onBindViewHolder(holder: VH, position: Int) {
            val app = items[position]
            holder.icon.setImageDrawable(app.icon)
            holder.label.text = app.label
            
            // Clear listener before setting checked state to avoid triggering onToggle
            holder.checkbox.setOnCheckedChangeListener(null)
            holder.checkbox.isChecked = app.packageName in selectedPkgs
            
            holder.checkbox.setOnCheckedChangeListener { _, checked ->
                onToggle(app.packageName, checked)
            }
            
            holder.itemView.setOnClickListener {
                holder.checkbox.isChecked = !holder.checkbox.isChecked
            }
        }

        class VH(view: View) : RecyclerView.ViewHolder(view) {
            val icon: ImageView = view.findViewById(R.id.iv_app_icon)
            val label: TextView = view.findViewById(R.id.tv_app_label)
            val checkbox: CheckBox = view.findViewById(R.id.cb_app)
        }
    }
}
