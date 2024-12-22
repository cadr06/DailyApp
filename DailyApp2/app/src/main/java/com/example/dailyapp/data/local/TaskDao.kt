package com.example.dailyapp.data.local

import androidx.room.* // Importation des annotations Room (Dao, Query, Insert, etc.)
import com.example.dailyapp.data.model.Task // Modèle représentant une tâche
import com.example.dailyapp.data.model.TaskStatus // Enum représentant le statut des tâches
import kotlinx.coroutines.flow.Flow // Pour les flux réactifs
import java.util.Date // Classe pour gérer les dates

// Interface DAO pour gérer les opérations CRUD sur les tâches
@Dao
interface TaskDao {

    // Insère une tâche dans la base de données et retourne son ID
    @Insert
    suspend fun insertTask(task: Task): Long

    // Met à jour une tâche existante
    @Update
    suspend fun updateTask(task: Task)

    // Supprime une tâche de la base de données
    @Delete
    suspend fun deleteTask(task: Task)

    // Récupère toutes les tâches d'un utilisateur triées par date d'échéance
    @Query("SELECT * FROM tasks WHERE userId = :userId ORDER BY dueDate ASC")
    fun getTasksForUser(userId: Long): Flow<List<Task>>

    // Récupère les tâches d'un utilisateur filtrées par statut, triées par date d'échéance
    @Query("SELECT * FROM tasks WHERE userId = :userId AND status = :status ORDER BY dueDate ASC")
    fun getTasksForUserByStatus(userId: Long, status: TaskStatus): Flow<List<Task>>

    // Récupère une tâche spécifique en fonction de son ID et de l'ID utilisateur
    @Query("SELECT * FROM tasks WHERE id = :taskId AND userId = :userId")
    suspend fun getTask(taskId: Long, userId: Long): Task?

    // Met à jour le statut d'une tâche spécifique
    @Query("UPDATE tasks SET status = :status WHERE id = :taskId AND userId = :userId")
    suspend fun updateTaskStatus(taskId: Long, userId: Long, status: TaskStatus)

    // Récupère les tâches à venir entre deux dates, pour lesquelles aucune notification n'a été envoyée
    @Query("""
        SELECT * FROM tasks 
        WHERE dueDate BETWEEN :start AND :end 
        AND notificationSent = 0 
        AND status != 'COMPLETED'
    """)
    suspend fun getUpcomingTasks(start: Date, end: Date): List<Task>

    // Met à jour l'état de notification d'une tâche
    @Query("UPDATE tasks SET notified = :status, notificationSent = :notificationSent WHERE id = :taskId")
    suspend fun updateTaskNotificationStatus(taskId: Long, status: Boolean, notificationSent: Boolean)

    // Compte les tâches non lues et non terminées
    @Query("SELECT COUNT(*) FROM tasks WHERE notified = 1 AND status != 'COMPLETED'")
    suspend fun getUnreadNotificationsCount(): Int

    // Récupère les tâches avec notifications, triées par date décroissante
    @Query("SELECT * FROM tasks WHERE notified = 1 ORDER BY dueDate DESC")
    fun getNotifiedTasks(): Flow<List<Task>>

    // Marque toutes les notifications comme lues
    @Query("UPDATE tasks SET notified = 0 WHERE notified = 1")
    suspend fun markAllNotificationsAsRead()
}
