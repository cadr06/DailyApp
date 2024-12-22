package com.example.dailyapp.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// Définition de la table "users" dans la base de données Room
@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)]
)
data class User(
    @PrimaryKey(autoGenerate = true)// Génère automatiquement un ID unique pour chaque utilisateur
    val id: Long = 0,// Identifiant unique de l'utilisateur
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String
)