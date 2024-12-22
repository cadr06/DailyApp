package com.example.dailyapp.data.local

import androidx.room.TypeConverter // Annotation pour définir les convertisseurs de types
import com.example.dailyapp.data.model.TaskStatus // Enumération représentant les statuts des tâches
import java.util.Date // Classe Java pour gérer les dates

// Classe Converters pour gérer les types complexes dans Room
class Converters {

    // Convertisseur pour les objets Date vers Long (timestamp)
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        // Si la valeur n'est pas nulle, convertit le timestamp en Date, sinon retourne null
        return value?.let { Date(it) }
    }

    // Convertisseur pour les objets Date vers leur équivalent Long (timestamp)
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        // Si la Date n'est pas nulle, retourne son temps en millisecondes, sinon retourne null
        return date?.time
    }

    // Convertisseur pour TaskStatus (Enum) vers une chaîne de caractères
    @TypeConverter
    fun fromTaskStatus(status: TaskStatus): String {
        // Retourne le nom de l'Enum en chaîne de caractères
        return status.name
    }

    // Convertisseur pour une chaîne de caractères vers TaskStatus (Enum)
    @TypeConverter
    fun toTaskStatus(value: String): TaskStatus {
        // Retourne l'Enum correspondant à la chaîne de caractères
        return TaskStatus.valueOf(value)
    }
}
