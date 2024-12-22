package com.example.dailyapp.ui

// Importations nécessaires
import com.example.dailyapp.adapters.NotificationsAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dailyapp.databinding.ActivityNotificationsBinding
import com.example.dailyapp.ui.tasks.TaskDetailActivity
import com.example.dailyapp.ui.tasks.TaskViewModel
import com.example.dailyapp.utils.NotificationService
import kotlinx.coroutines.launch

class NotificationsActivity : AppCompatActivity() {

    // Initialisation des variables
    private lateinit var binding: ActivityNotificationsBinding // Liaison avec le layout XML
    private val viewModel: TaskViewModel by viewModels() // Instance du ViewModel pour gérer les données
    private lateinit var adapter: NotificationsAdapter // Adaptateur pour le RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configuration de la vue avec ViewBinding
        binding = ActivityNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialisations nécessaires
        initAdapter() // Initialiser l'adaptateur
        setupToolbar() // Configurer la barre d'outils
        setupRecyclerView() // Configurer le RecyclerView
        observeNotifications() // Observer les notifications
    }

    // Méthode pour initialiser l'adaptateur
    private fun initAdapter() {
        adapter = NotificationsAdapter { task ->
            // Lorsqu'une notification est cliquée, ouvrir les détails de la tâche
            val intent = Intent(this, TaskDetailActivity::class.java)
            intent.putExtra("task_id", task.id) // Passer l'ID de la tâche en extra
            startActivity(intent) // Démarrer l'activité des détails de la tâche
        }
    }

    // Afficher l'état vide (aucune notification)
    private fun showEmptyState() {
        binding.emptyState.visibility = View.VISIBLE // Afficher le texte ou l'image de l'état vide
        binding.notificationsRecyclerView.visibility = View.GONE // Masquer le RecyclerView
    }

    // Masquer l'état vide (des notifications existent)
    private fun hideEmptyState() {
        binding.emptyState.visibility = View.GONE // Masquer le texte ou l'image de l'état vide
        binding.notificationsRecyclerView.visibility = View.VISIBLE // Afficher le RecyclerView
    }

    // Observer les notifications à partir du ViewModel
    private fun observeNotifications() {
        lifecycleScope.launch {
            viewModel.getNotifiedTasks().collect { tasks -> // Collecter les tâches notifiées via Flow
                Log.d("NotificationsActivity", "Nombre de tâches reçues : ${tasks.size}") // Journaliser le nombre de tâches
                if (tasks.isEmpty()) { // Si aucune tâche notifiée
                    showEmptyState() // Afficher l'état vide
                } else {
                    hideEmptyState() // Masquer l'état vide
                    adapter.submitList(tasks) // Soumettre la liste des tâches à l'adaptateur
                }
            }
        }
    }

    // Configurer la barre d'outils
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar) // Définir la barre d'outils
        supportActionBar?.apply {
            title = "Notifications" // Définir le titre de la barre d'outils
            setDisplayHomeAsUpEnabled(true) // Activer le bouton de retour
        }
    }

    // Configurer le RecyclerView pour afficher les notifications
    private fun setupRecyclerView() {
        binding.notificationsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@NotificationsActivity) // Disposition en liste verticale
            this.adapter = this@NotificationsActivity.adapter // Assigner l'adaptateur
        }
    }

    // Méthode appelée lorsque l'activité est reprise
    override fun onResume() {
        super.onResume()
        observeNotifications() // Réobserver les notifications pour mettre à jour l'affichage
    }

    // Méthode appelée lorsque l'activité est mise en pause
    override fun onPause() {
        super.onPause()
        lifecycleScope.launch {
            // Marquer toutes les notifications comme lues dans le ViewModel
            viewModel.markAllNotificationsAsRead()
            // Envoyer une diffusion pour mettre à jour le nombre de notifications non lues
            sendBroadcast(Intent(NotificationService.ACTION_UPDATE_NOTIFICATION_COUNT).apply {
                putExtra(NotificationService.EXTRA_COUNT, 0) // Remettre le compteur à zéro
            })
        }
    }

    // Gérer le bouton de retour dans la barre d'outils
    override fun onSupportNavigateUp(): Boolean {
        finish() // Terminer l'activité actuelle
        return true
    }
}