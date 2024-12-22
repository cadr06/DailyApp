package com.example.dailyapp.ui.tasks

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.dailyapp.R
import com.example.dailyapp.data.model.Task
import com.example.dailyapp.data.model.TaskStatus
import com.example.dailyapp.databinding.ActivityTaskDetailBinding
import com.example.dailyapp.utils.SessionManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.util.Locale
import java.text.SimpleDateFormat


class TaskDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTaskDetailBinding
    private val viewModel: TaskViewModel by viewModels()
    private var taskId: Long = -1
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        
        taskId = intent.getLongExtra("task_id", -1)
        if (taskId == -1L) {
            finish()
            return
        }

        loadTask()
        setupButtons()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Détails de la tâche"
    }

    private fun loadTask() {
        lifecycleScope.launch {
            val task = viewModel.getTask(taskId, SessionManager.getUserId(this@TaskDetailActivity))
            task?.let { displayTask(it) }
        }
    }

    private fun displayTask(task: Task) {
        binding.apply {
            taskTitle.text = task.title
            taskDescription.text = task.description
            taskDueDate.text = dateFormat.format(task.dueDate)
            
            taskStatus.text = when(task.status) {
                TaskStatus.PENDING -> "En attente"
                TaskStatus.IN_PROGRESS -> "En cours"
                TaskStatus.COMPLETED -> "Terminée"
            }

            if (task.latitude == null || task.longitude == null) {
                navigateButton.visibility = View.GONE
                mapView.visibility = View.GONE
            } else {
                navigateButton.visibility = View.VISIBLE
                mapView.visibility = View.VISIBLE
            }

            setupStatusChip(task)
            setupNavigationButton(task)
            setupMap(task)
        }
    }

    private fun setupNavigationButton(task: Task) {
    binding.navigateButton.setOnClickListener {
        if (task.latitude != null && task.longitude != null && task.address != null) {
            // Navigation avec coordonnées
            val uri = Uri.parse("google.navigation:q=${task.latitude},${task.longitude}")
            val mapIntent = Intent(Intent.ACTION_VIEW, uri)
            mapIntent.setPackage("com.google.android.apps.maps")
            startActivity(mapIntent)
        }


    }
    }
private fun setupMap(task: Task) {
    val mapFragment = supportFragmentManager
        .findFragmentById(R.id.mapView) as SupportMapFragment
    
    mapFragment.getMapAsync { googleMap ->
        if (task.latitude != null && task.longitude != null) {
            val taskLocation = LatLng(task.latitude, task.longitude)
            googleMap.addMarker(MarkerOptions().position(taskLocation))
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(taskLocation, 15f))
        }
    }
}
    private fun setupButtons() {
        binding.editButton.setOnClickListener {
            val intent = Intent(this, AddEditTaskActivity::class.java)
            intent.putExtra("task_id", taskId)
            startActivity(intent)
            finish()
        }

        binding.deleteButton.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }

    private fun setupStatusChip(task: Task) {
        binding.taskStatus.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Changer le statut")
                .setItems(arrayOf("En attente", "En cours", "Terminée")) { _, which ->
                    val newStatus = when (which) {
                        0 -> TaskStatus.PENDING
                        1 -> TaskStatus.IN_PROGRESS
                        2 -> TaskStatus.COMPLETED
                        else -> task.status
                    }
                    viewModel.updateTaskStatus(task, newStatus)
                    lifecycleScope.launch {
                        viewModel.updateTaskStatus(task, newStatus)
                        task.copy(status = newStatus).also { updatedTask ->
                            displayTask(updatedTask)
                        }
                    }

                }
                .show()
        }
    }

    private fun showDeleteConfirmationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Supprimer la tâche")
            .setMessage("Êtes-vous sûr de vouloir supprimer cette tâche ?")
            .setPositiveButton("Supprimer") { _, _ ->
                lifecycleScope.launch {
                    val task = viewModel.getTask(taskId, SessionManager.getUserId(this@TaskDetailActivity))
                    task?.let {
                        viewModel.deleteTask(it)
                        finish()
                    }
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}