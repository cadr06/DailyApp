package com.example.dailyapp.data.local

import android.content.Context // Fournit le contexte de l'application pour initialiser la base de données
import androidx.room.Database // Annotation pour définir une base de données Room
import androidx.room.Room // Classe pour créer une instance de base de données Room
import androidx.room.RoomDatabase // Classe de base pour les bases de données Room
import androidx.room.TypeConverters // Permet de convertir des types complexes pour Room
import com.example.dailyapp.data.model.Task // Modèle de données représentant une tâche
import com.example.dailyapp.data.model.User // Modèle de données représentant un utilisateur

// Annotation @Database pour définir les entités (tables) et la version de la base
@Database(entities = [User::class, Task::class], version = 1, exportSchema = false)
// Utilisation de TypeConverters pour les types complexes (e.g., Date, Enum)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    // Méthodes abstraites pour accéder aux DAO
    abstract fun userDao(): UserDao // Accès aux opérations sur les utilisateurs
    abstract fun taskDao(): TaskDao // Accès aux opérations sur les tâches

    companion object {
        @Volatile // Assure la visibilité des modifications entre threads
        private var INSTANCE: AppDatabase? = null // Instance unique de la base de données

        // Méthode pour obtenir l'instance de la base de données
        fun getDatabase(context: Context): AppDatabase {
            // Si INSTANCE est null, on la crée dans un bloc synchronized
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext, // Utilise le contexte d'application
                    AppDatabase::class.java, // Classe de la base de données
                    "daily_app_database" // Nom du fichier de la base de données
                ).build() // Crée l'instance de la base
                INSTANCE = instance // Stocke l'instance
                instance // Retourne l'instance
            }
        }
    }
}
