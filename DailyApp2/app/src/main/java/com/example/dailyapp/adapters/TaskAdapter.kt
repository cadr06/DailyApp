package com.example.dailyapp.adapters

import android.content.Intent // Pour démarrer une activité avec une intention explicite
import android.net.Uri // Pour gérer des URIs comme les adresses géographiques
import android.view.LayoutInflater // Pour gonfler les layouts XML
import android.view.View // Représente une vue dans l'interface utilisateur
import android.view.ViewGroup // Groupe de vues parent
import android.widget.PopupMenu // Menu contextuel (popup) pour des actions supplémentaires
import androidx.recyclerview.widget.DiffUtil // Utilisé pour optimiser les mises à jour des listes
import androidx.recyclerview.widget.ListAdapter // Adapter basé sur DiffUtil
import androidx.recyclerview.widget.RecyclerView // Classe de base pour les adaptateurs RecyclerView
import com.example.dailyapp.R // Accès aux ressources (menus, layouts, etc.)
import com.example.dailyapp.data.model.Task // Modèle de données représentant une tâche
import com.example.dailyapp.data.model.TaskStatus // Enumération des différents statuts de tâche
import com.example.dailyapp.databinding.ItemTaskBinding // Liaison avec le layout XML ItemTask
import java.text.SimpleDateFormat // Pour formater les dates
import java.util.Locale // Gère les paramètres régionaux

// Adapter pour afficher une liste de tâches avec différentes interactions
class TaskAdapter(
    private val onTaskClick: (Task) -> Unit, // Callback lors du clic sur une tâche
    private val onStatusClick: (Task) -> Unit, // Callback lors du clic sur le statut d'une tâche
    private val onDeleteClick: (Task) -> Unit, // Callback lors de la suppression d'une tâche
    private val onEditClick: (Task) -> Unit // Callback lors de l'édition d'une tâche
) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(TaskDiffCallback()) {
    // Hérite de ListAdapter pour gérer efficacement les listes avec DiffUtil

    // Crée un ViewHolder pour un élément de tâche
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    // Lie les données de la position actuelle au ViewHolder
    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    // ViewHolder pour chaque tâche
    inner class TaskViewHolder(
        private val binding: ItemTaskBinding // Utilisation de View Binding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        // Format pour les dates et heures

        fun bind(task: Task) {
            // Lie les données de la tâche aux vues du layout
            binding.apply {
                taskTitle.text = task.title // Titre de la tâche
                taskDescription.text = task.description // Description de la tâche
                taskDueDate.text = dateFormat.format(task.dueDate) // Date limite de la tâche

                // Définit le statut de la tâche
                taskStatus.text = when (task.status) {
                    TaskStatus.PENDING -> "En attente"
                    TaskStatus.IN_PROGRESS -> "En cours"
                    TaskStatus.COMPLETED -> "Terminée"
                }

                // Bouton pour afficher la localisation si elle existe
                taskLocation.visibility = if (task.address != null) View.VISIBLE else View.GONE
                taskLocation.setOnClickListener {
                    task.address?.let { address ->
                        val uri = Uri.parse("geo:0,0?q=${Uri.encode(address)}") // URI pour afficher l'adresse
                        val mapIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                            setPackage("com.google.android.apps.maps") // Spécifie Google Maps
                        }
                        itemView.context.startActivity(mapIntent) // Démarre Google Maps
                    }
                }

                // Configure les actions sur les clics
                root.setOnClickListener { onTaskClick(task) } // Clic sur l'élément
                taskStatus.setOnClickListener { onStatusClick(task) } // Clic sur le statut

                // Menu contextuel sur un clic long
                root.setOnLongClickListener { view ->
                    showPopupMenu(view, task) // Affiche un menu contextuel
                    true
                }
            }
        }

        private fun showPopupMenu(view: View, task: Task) {
            // Affiche un menu contextuel avec des options d'édition et de suppression
            PopupMenu(view.context, view).apply {
                inflate(R.menu.task_menu) // Gonfle le menu depuis le fichier XML
                setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.action_edit -> {
                            onEditClick(task) // Action d'édition
                            true
                        }
                        R.id.action_delete -> {
                            onDeleteClick(task) // Action de suppression
                            true
                        }
                        else -> false
                    }
                }
                show() // Affiche le menu
            }
        }
    }

    // DiffUtil pour optimiser les mises à jour de la liste
    class TaskDiffCallback : DiffUtil.ItemCallback<Task>() {
        // Vérifie si deux tâches sont les mêmes
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem.id == newItem.id
        }

        // Vérifie si le contenu de deux tâches est identique
        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem == newItem
        }
    }
}
