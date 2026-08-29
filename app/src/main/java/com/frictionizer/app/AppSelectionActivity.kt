package com.frictionizer.app

import android.content.pm.ApplicationInfo
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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
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

    private var curatedApps: List<AppInfo> = emptyList()
    private var allSystemApps: List<AppInfo> = emptyList()

    // Known time-waster package names
    private val KNOWN_TIME_WASTERS = setOf(
        // Social media
        "com.facebook.katana", "com.facebook.lite", "com.facebook.mlite",
        "com.instagram.android",
        "com.twitter.android", "com.twitter.android.lite",
        "com.snapchat.android",
        "com.zhiliaoapp.musically", "com.ss.android.ugc.trill",  // TikTok
        "com.reddit.frontpage",
        "com.linkedin.android",
        "com.pinterest",
        "com.tumblr",
        "com.discord",
        "com.BeReal.app",
        "com.threads.lite", "com.instagram.barcelona",           // Threads
        "xyz.blueskyweb.app",                                    // Bluesky
        "com.vk.vkclient",
        // Video & streaming
        "com.google.android.youtube",
        "com.google.android.youtube.kids",
        "com.netflix.mediaclient",
        "com.amazon.avod.thirdpartyclient",
        "com.disneyplus",
        "com.hulu.plus",
        "com.hbo.hbonow",
        "com.max.android",
        "tv.twitch.android.app",
        "com.twitch.tv",
        "nz.co.spark.sparksport",
        "nz.co.tvnz.ondemand",
        "com.tvnz.tvnzplus",
        "au.com.stan.stan",
        // Music (can be time-sinks)
        "com.spotify.music",
        "com.apple.android.music",
        "com.soundcloud.android",
        "com.pandora.android",
        // Games (common time-wasters)
        "com.king.candycrushsaga",
        "com.king.candycrushsodasaga",
        "com.supercell.clashofclans",
        "com.supercell.clashroyale",
        "com.supercell.brawlstars",
        "com.roblox.client",
        "com.mojang.minecraftpe",
        "com.gameloft.android.ANMP.GloftA9HM",
        "com.halfbrick.fruitninjafree",
        "com.ea.game.pvzfree_row",
        "com.playrix.gardenscapes",
        "com.playrix.homescapes",
        "com.zynga.farmville3",
        "com.niantic.pokemongo",
        "com.kiloo.subwaysurf",
        "com.imangi.templerun2",
        // Gambling / betting
        "com.bet365.mobile",
        "com.sportsbet",
        "nz.co.tab.mobile",
        // Browsers (rabbit holes)
        "com.android.chrome",
        "org.mozilla.firefox",
        "com.microsoft.emmx",
        "com.opera.browser",
        "com.brave.browser",
        "com.duckduckgo.mobile.android",
        "com.sec.android.app.sbrowser",
        // Shopping
        "com.amazon.mShop.android.shopping",
        "com.ebay.mobile",
        "com.wish.android"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAppSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(
                top = systemBars.top,
                bottom = systemBars.bottom
            )
            insets
        }

        selectedApps.addAll(PrefsHelper.getMonitoredApps(this))
        adapter = AppAdapter(selectedApps) { pkg, checked ->
            if (checked) selectedApps.add(pkg) else selectedApps.remove(pkg)
            PrefsHelper.setMonitoredApps(this, selectedApps)
            refreshDisplayList()
        }

        binding.recyclerApps.layoutManager = LinearLayoutManager(this)
        binding.recyclerApps.adapter = adapter

        // "Add other app" button shows searchable full list dialog
        binding.btnAddOther.setOnClickListener { showAllAppsDialog() }

        // Search within the curated list OR global search if text is present
        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrBlank()) {
                    refreshDisplayList()
                } else {
                    adapter.filterGlobal(newText, allSystemApps)
                }
                return true
            }
        })

        CoroutineScope(Dispatchers.Main).launch {
            binding.progressBar.visibility = View.VISIBLE
            val curated = withContext(Dispatchers.IO) { loadCuratedApps() }
            allSystemApps = withContext(Dispatchers.IO) { loadAllAppsIncludingSystem() }
            curatedApps = curated
            binding.progressBar.visibility = View.GONE
            refreshDisplayList()
        }
    }

    private fun refreshDisplayList() {
        // Show curated apps PLUS any other apps that are currently selected
        val extraSelected = allSystemApps.filter { 
            it.packageName in selectedApps && it.packageName !in KNOWN_TIME_WASTERS 
        }
        val combined = (curatedApps + extraSelected).distinctBy { it.packageName }.sortedBy { it.label }
        adapter.setItems(combined)
        binding.tvEmpty.visibility = if (combined.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }

    private fun loadCuratedApps(): List<AppInfo> {
        val pm = packageManager
        return pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { app ->
                app.packageName in KNOWN_TIME_WASTERS &&
                pm.getLaunchIntentForPackage(app.packageName) != null
            }
            .map { app ->
                AppInfo(app.packageName, app.loadLabel(pm).toString(), app.loadIcon(pm))
            }
            .sortedBy { it.label }
    }

    private fun loadAllAppsIncludingSystem(): List<AppInfo> {
        val pm = packageManager
        return pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { app ->
                pm.getLaunchIntentForPackage(app.packageName) != null &&
                app.packageName != packageName
            }
            .map { app ->
                AppInfo(app.packageName, app.loadLabel(pm).toString(), app.loadIcon(pm))
            }
            .sortedBy { it.label }
    }

    private fun loadAllOtherApps(): List<AppInfo> {
        val pm = packageManager
        return pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { app ->
                app.packageName !in KNOWN_TIME_WASTERS &&
                app.packageName != packageName &&
                pm.getLaunchIntentForPackage(app.packageName) != null &&
                (app.flags and ApplicationInfo.FLAG_SYSTEM) == 0
            }
            .map { app ->
                AppInfo(app.packageName, app.loadLabel(pm).toString(), app.loadIcon(pm))
            }
            .sortedBy { it.label }
    }

    private fun showAllAppsDialog() {
        val loading = AlertDialog.Builder(this)
            .setTitle("Loading apps…")
            .setMessage("Please wait")
            .setCancelable(false)
            .show()

        CoroutineScope(Dispatchers.Main).launch {
            val otherApps = withContext(Dispatchers.IO) { loadAllOtherApps() }
            loading.dismiss()

            val labels = otherApps.map { it.label }.toTypedArray()
            val checked = otherApps.map { it.packageName in selectedApps }.toBooleanArray()

            AlertDialog.Builder(this@AppSelectionActivity)
                .setTitle("Other installed apps")
                .setMultiChoiceItems(labels, checked) { _, which, isChecked ->
                    val pkg = otherApps[which].packageName
                    if (isChecked) selectedApps.add(pkg) else selectedApps.remove(pkg)
                    PrefsHelper.setMonitoredApps(this@AppSelectionActivity, selectedApps)
                    refreshDisplayList()
                }
                .setPositiveButton("Done", null)
                .show()
        }
    }

    data class AppInfo(val packageName: String, val label: String, val icon: Drawable)

    class AppAdapter(
        private val selectedPkgs: Set<String>,
        private val onToggle: (String, Boolean) -> Unit
    ) : RecyclerView.Adapter<AppAdapter.VH>() {

        private var allItems: List<AppInfo> = emptyList()
        private var displayItems: List<AppInfo> = emptyList()

        fun setItems(list: List<AppInfo>) {
            allItems = list; displayItems = list; notifyDataSetChanged()
        }

        fun filterGlobal(query: String, allApps: List<AppInfo>) {
            displayItems = allApps.filter { it.label.contains(query, ignoreCase = true) }
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, vt: Int) = VH(
            LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false)
        )
        override fun getItemCount() = displayItems.size
        override fun onBindViewHolder(h: VH, pos: Int) {
            val app = displayItems[pos]
            h.icon.setImageDrawable(app.icon)
            h.label.text = app.label
            h.checkbox.setOnCheckedChangeListener(null)
            h.checkbox.isChecked = app.packageName in selectedPkgs
            h.checkbox.setOnCheckedChangeListener { _, checked -> onToggle(app.packageName, checked) }
            h.itemView.setOnClickListener { h.checkbox.isChecked = !h.checkbox.isChecked }
        }

        class VH(view: View) : RecyclerView.ViewHolder(view) {
            val icon: ImageView = view.findViewById(R.id.iv_app_icon)
            val label: TextView = view.findViewById(R.id.tv_app_label)
            val checkbox: CheckBox = view.findViewById(R.id.cb_app)
        }
    }
}
