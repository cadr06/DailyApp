package com.example.dailyapp.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dailyapp.R
import com.example.dailyapp.adapters.TaskAdapter
import com.example.dailyapp.data.model.Task
import com.example.dailyapp.data.model.TaskStatus
import com.example.dailyapp.databinding.ActivityMainBinding
import com.example.dailyapp.ui.auth.LoginActivity
import com.example.dailyapp.ui.tasks.AddEditTaskActivity

import com.example.dailyapp.ui.tasks.TaskDetailActivity
import com.example.dailyapp.ui.tasks.TaskViewModel
import com.example.dailyapp.utils.NotificationService
import com.example.dailyapp.utils.SessionManager
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils
import com.google.android.material.badge.ExperimentalBadgeUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    // Liaison avec la vue via le binding
    private lateinit var binding: ActivityMainBinding
    private val viewModel: TaskViewModel by viewModels()  // ViewModel pour gérer les tâches
    private lateinit var taskAdapter: TaskAdapter  // Adaptateur pour gérer l'affichage des tâches dans le RecyclerView
    private var notificationMenuItem: MenuItem? = null  // MenuItem pour l'icône de notification

    private lateinit var notificationBadge: BadgeDrawable// Badge pour afficher le nombre de notifications

    // Récepteur pour écouter les mises à jour du nombre de notifications
    private val notificationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == NotificationService.ACTION_UPDATE_NOTIFICATION_COUNT) {
                val count = intent.getIntExtra(NotificationService.EXTRA_COUNT, 0)
                Log.d("MainActivity", "Received notification count: $count")
                updateNotificationBadge(count)// Mise à jour du badge avec le nouveau compte de notifications
            }
        }
    }

    // Fonction pour récupérer le nombre de notifications non lues et mettre à jour le badge
     private fun updateNotificationCount() {
        lifecycleScope.launch {
            val count = viewModel.getUnreadNotificationsCount()
            updateNotificationBadge(count)
        }
    }

    // Appelée quand l'activité reprend (après avoir été mise en pause ou autre)
    override fun onResume() {
        super.onResume()
        updateNotificationCount()// Mise à jour du compteur de notifications à chaque reprise de l'activité
    }

    // Initialisation de l'activité
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupTabs()
        setupRecyclerView()
        setupFab()
        observeTasks()
        setupNotificationBadge()
        registerNotificationReceiver()

        // Demander la permission de notifications si l'Android est supérieur à TIRAMISU (version 33)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission()
        }

        // Démarrer le service de notification pour recevoir des mises à jour
        startService(Intent(this, NotificationService::class.java))

        // Enregistrer le récepteur pour recevoir les mises à jour du nombre de notifications
        registerReceiver(
        notificationReceiver,
        IntentFilter(NotificationService.ACTION_UPDATE_NOTIFICATION_COUNT),
        RECEIVER_NOT_EXPORTED
    )

    }

    // Enregistrer le récepteur pour recevoir les mises à jour du nombre de notifications
    @RequiresApi(Build.VERSION_CODES.O)
    private fun registerNotificationReceiver() {
        registerReceiver(
            notificationReceiver,
            IntentFilter(NotificationService.ACTION_UPDATE_NOTIFICATION_COUNT),
            RECEIVER_NOT_EXPORTED
        )
    }

    // Demander la permission pour envoyer des notifications sur Android 13 ou supérieur
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestNotificationPermission() {
    if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != 
        PackageManager.PERMISSION_GRANTED) {
        requestPermissions(
            arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
            100
        )
        }
    }

    // Appelée lors de la destruction de l'activité pour nettoyer les ressources
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(notificationReceiver)
    }

    // Configuration de la toolbar
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "DailyApp"
            subtitle = SessionManager.getUserEmail(this@MainActivity)
        }

        updateNotificationCount()
    }

        private fun setupNotificationBadge() {
        // Créer le badge
        notificationBadge = BadgeDrawable.create(this)
        // Configurer le badge
        notificationBadge.apply {
            backgroundColor = ContextCompat.getColor(this@MainActivity, R.color.red) // ou une autre couleur de votre choix
            badgeTextColor = ContextCompat.getColor(this@MainActivity, R.color.white)
            isVisible = false // Caché par défaut
        }
    }

    // Créer et configurer le menu d'options avec le badge de notifications
     @OptIn(ExperimentalBadgeUtils::class)
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        notificationMenuItem = menu.findItem(R.id.action_notifications)

         // Mettre à jour le badge avec le nombre de notifications
        notificationMenuItem?.let { menuItem ->
            notificationBadge = BadgeDrawable.create(this)
            notificationBadge.backgroundColor = ContextCompat.getColor(this, R.color.red)
            notificationBadge.badgeTextColor = ContextCompat.getColor(this, R.color.white)
            BadgeUtils.attachBadgeDrawable(notificationBadge, binding.toolbar, menuItem.itemId)

            // Mettre à jour le badge avec le nombre de notifications
            lifecycleScope.launch {
                val count = viewModel.getUnreadNotificationsCount()
                updateNotificationBadge(count)
            }
        }
        
        return true
    }

    // Mettre à jour le badge avec le nombre de notifications
    fun updateNotificationBadge(count: Int) {
        lifecycleScope.launch(Dispatchers.Main) {
            notificationBadge.apply {
                number = count// Définir le nombre de notifications
                isVisible = count > 0// Rendre le badge visible si le nombre est supérieur à 0
            }
            Log.d("MainActivity", "Updated badge count: $count")
        }
    }


    private fun setupTabs() {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("En attente"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("En cours"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Terminées"))

        binding.tabLayout.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> viewModel.setFilter(TaskStatus.PENDING)
                    1 -> viewModel.setFilter(TaskStatus.IN_PROGRESS)
                    2 -> viewModel.setFilter(TaskStatus.COMPLETED)
                }
            }
            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
        })
    }

    // Configurer le RecyclerView pour afficher les tâches
    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(
            onTaskClick = { task -> onTaskClick(task) },// Action de clic sur une tâche
            onStatusClick = { task -> showStatusChangeDialog(task) },// Action de clic sur le statut d'une tâche
            onDeleteClick = { task -> showDeleteConfirmationDialog(task) },// Action de clic pour supprimer une tâche

            onEditClick = { task -> onTaskClick(task) }// Action de clic pour éditer une tâche
        )

        binding.tasksRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)// Utiliser une disposition verticale pour le RecyclerView
            adapter = taskAdapter// Attacher l'adaptateur
        }
    }

    // Configurer le Floating Action Button (FAB) pour ajouter une nouvelle tâche
    private fun setupFab() {
        binding.addTaskFab.setOnClickListener {
            startActivity(Intent(this, AddEditTaskActivity::class.java))
        }
    }

    // Observer les tâches et mettre à jour l'adaptateur lorsque les données changent
    private fun observeTasks() {
        lifecycleScope.launch {
            viewModel.tasks.collect { tasks ->// Observer la liste des tâches
                taskAdapter.submitList(tasks)// Mettre à jour l'adaptateur avec la nouvelle liste de tâches
            }
        }
    }

    private fun onTaskClick(task: Task) {
        val intent = Intent(this, TaskDetailActivity::class.java)
        intent.putExtra("task_id", task.id) // Passer l'ID de la tâche pour afficher les détails
        startActivity(intent)
    }
    // Afficher un dialogue de confirmation pour supprimer une tâche
    private fun showDeleteConfirmationDialog(task: Task) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Supprimer la tâche")
            .setMessage("Êtes-vous sûr de vouloir supprimer cette tâche ?")
            .setPositiveButton("Supprimer") { _, _ ->
                viewModel.deleteTask(task)// Supprimer la tâche si confirmé
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    // Afficher un dialogue pour changer le statut d'une tâche
    private fun showStatusChangeDialog(task: Task) {
        val statuses = TaskStatus.values()
        val statusNames = statuses.map { status ->// Créer un tableau des noms des statuts
            when (status) {
                TaskStatus.PENDING -> "En attente"
                TaskStatus.IN_PROGRESS -> "En cours"
                TaskStatus.COMPLETED -> "Terminée"
            }
        }.toTypedArray()

        MaterialAlertDialogBuilder(this)
            .setTitle("Changer le statut")
            .setItems(statusNames) { _, which ->// Changer le statut de la tâche
                viewModel.updateTaskStatus(task, statuses[which])
            }
            .show()
    }


    // Gérer les actions du menu (notifications, déconnexion)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_notifications -> {
            startActivity(Intent(this, NotificationsActivity::class.java))// Ouvrir l'écran des notifications
            true
            }
            R.id.action_logout -> {
                logout()// Déconnecter l'utilisateur
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // Déconnecter l'utilisateur et rediriger vers l'écran de connexion
    private fun logout() {
        SessionManager.clearSession(this)// Effacer la session
        startActivity(Intent(this, LoginActivity::class.java)) // Rediriger vers la page de connexion
        finish()// Fermer cette activité
    }
}