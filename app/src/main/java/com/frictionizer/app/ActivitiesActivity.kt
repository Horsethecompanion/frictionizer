package com.frictionizer.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.frictionizer.app.databinding.ActivityActivitiesBinding
import com.frictionizer.app.utils.PrefsHelper

class ActivitiesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityActivitiesBinding
    private lateinit var adapter: ActivityAdapter
    private val activities = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityActivitiesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Handle insets
        ViewCompat.setOnApplyWindowInsetsListener(binding.activitiesRoot) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(
                left = systemBars.left,
                top = systemBars.top,
                right = systemBars.right,
                bottom = systemBars.bottom
            )
            insets
        }

        activities.addAll(PrefsHelper.getActivities(this))
        adapter = ActivityAdapter(activities,
            onEdit = { pos -> showEditDialog(pos) },
            onDelete = { pos ->
                activities.removeAt(pos)
                adapter.notifyItemRemoved(pos)
                save()
            }
        )

        binding.recyclerActivities.layoutManager = LinearLayoutManager(this)
        binding.recyclerActivities.adapter = adapter

        val touchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
        ) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                val from = vh.adapterPosition
                val to = target.adapterPosition
                val item = activities.removeAt(from)
                activities.add(to, item)
                adapter.notifyItemMoved(from, to)
                save()
                return true
            }
            override fun onSwiped(vh: RecyclerView.ViewHolder, dir: Int) {}
        })
        touchHelper.attachToRecyclerView(binding.recyclerActivities)

        binding.fabAddActivity.setOnClickListener {
            showEditDialog(-1)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun showEditDialog(position: Int) {
        val isNew = position < 0
        val editText = EditText(this).apply {
            hint = "e.g. On treadmill"
            if (!isNew) setText(activities[position])
            setPadding(48, 24, 48, 24)
        }
        AlertDialog.Builder(this)
            .setTitle(if (isNew) "Add Activity" else "Edit Activity")
            .setView(editText)
            .setPositiveButton(if (isNew) "Add" else "Save") { _, _ ->
                val text = editText.text.toString().trim()
                if (text.isNotEmpty()) {
                    if (isNew) {
                        activities.add(text)
                        adapter.notifyItemInserted(activities.size - 1)
                    } else {
                        activities[position] = text
                        adapter.notifyItemChanged(position)
                    }
                    save()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun save() = PrefsHelper.setActivities(this, activities)

    class ActivityAdapter(
        private val items: List<String>,
        private val onEdit: (Int) -> Unit,
        private val onDelete: (Int) -> Unit
    ) : RecyclerView.Adapter<ActivityAdapter.VH>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
            LayoutInflater.from(parent.context).inflate(R.layout.item_activity, parent, false)
        )
        override fun getItemCount() = items.size
        override fun onBindViewHolder(holder: VH, position: Int) {
            holder.label.text = items[position]
            holder.editBtn.setOnClickListener { onEdit(holder.adapterPosition) }
            holder.deleteBtn.setOnClickListener { onDelete(holder.adapterPosition) }
        }

        class VH(view: View) : RecyclerView.ViewHolder(view) {
            val label: TextView = view.findViewById(R.id.tv_activity_name)
            val editBtn: ImageButton = view.findViewById(R.id.btn_edit)
            val deleteBtn: ImageButton = view.findViewById(R.id.btn_delete)
        }
    }
}
