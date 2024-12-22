package com.example.dailyapp.adapters

import androidx.recyclerview.widget.ListAdapter // Permet d'utiliser des listes différentielles pour optimiser les mises à jour
import android.view.LayoutInflater // Pour inflater (gonfler) les layouts XML
import android.view.ViewGroup // Représente un groupe de vues dans la hiérarchie
import androidx.recyclerview.widget.RecyclerView // Classe de base pour créer un adaptateur RecyclerView
import com.example.dailyapp.data.model.Task // Importation du modèle de données Task
import com.example.dailyapp.databinding.ItemNotificationBinding // Liaison avec le layout XML ItemNotification

import java.text.SimpleDateFormat // Classe pour formater les dates
import java.util.Locale // Fournit des paramètres régionaux

class NotificationsAdapter(
    private val onNotificationClick: (Task) -> Unit // Callback déclenché lors d'un clic sur une notification
) : ListAdapter<Task, NotificationsAdapter.NotificationViewHolder>(TaskAdapter.TaskDiffCallback()) {
    // Hérite de ListAdapter pour gérer une liste de tâches avec des diff callbacks

    // Classe interne représentant le ViewHolder pour une notification
    inner class NotificationViewHolder(
        private val binding: ItemNotificationBinding // Utilisation de View Binding pour accéder aux vues du layout
    ) : RecyclerView.ViewHolder(binding.root) { // Hérite de RecyclerView.ViewHolder

        private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        // Format utilisé pour afficher les dates et heures

        fun bind(task: Task) {
            // Méthode pour lier les données d'une tâche aux vues
            binding.apply {
                notificationTitle.text = task.title // Définit le titre de la tâche
                notificationTime.text = dateFormat.format(task.dueDate) // Affiche la date au format défini
                notificationDescription.text = task.description // Définit la description de la tâche

                root.setOnClickListener { onNotificationClick(task) }
                // Déclenche le callback lors d'un clic sur l'élément
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        // Méthode appelée pour créer un ViewHolder
        val binding = ItemNotificationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        // Crée une instance de ItemNotificationBinding en gonflant le layout
        return NotificationViewHolder(binding)
        // Retourne un nouveau ViewHolder
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        // Méthode appelée pour lier les données d'une position donnée au ViewHolder
        holder.bind(getItem(position)) // Récupère la tâche à la position et la lie au ViewHolder
    }
}
