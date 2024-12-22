package com.example.dailyapp.data.local

import androidx.room.* // Importation des annotations Room
import com.example.dailyapp.data.model.User // Modèle représentant un utilisateur

// Interface DAO pour effectuer des opérations sur la table des utilisateurs
@Dao
interface UserDao {

    // Insère un nouvel utilisateur dans la base de données
    @Insert
    suspend fun insertUser(user: User): Long
    // Retourne l'ID de l'utilisateur inséré, généré automatiquement par la base de données

    // Récupère un utilisateur à partir de son email et mot de passe
    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    suspend fun getUser(email: String, password: String): User?
    // Utilisé pour authentifier un utilisateur. Retourne `null` si aucun utilisateur ne correspond.

    // Vérifie si un email existe déjà dans la base de données
    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE email = :email)")
    suspend fun isEmailExists(email: String): Boolean
    // Retourne `true` si un email est déjà présent, sinon `false`.
}
