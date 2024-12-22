package com.example.dailyapp.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.Date

// Définition de la table "tasks" dans la base de données Room
@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Task(
    @PrimaryKey(autoGenerate = true)// Génère automatiquement un ID unique pour chaque tâche
    val id: Long = 0,
    
    val userId: Long,  // Lien avec l'utilisateur
    val title: String,
    val description: String,
    val dueDate: Date,  // Date limite
    val status: TaskStatus = TaskStatus.PENDING,  // État de la tâche
    val address: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val notified: Boolean = false,// Indique si l'utilisateur a été notifié pour cette tâche
    val notificationSent: Boolean = false,
)

enum class TaskStatus {
    PENDING,   // En attente
    IN_PROGRESS,  // En cours
    COMPLETED   // Terminée
}