package com.example.dailyapp.utils

import android.content.Context
import android.content.SharedPreferences

object SessionManager {
    // Déclaration des constantes pour les clés utilisées dans les SharedPreferences
    private const val PREF_NAME = "DailyAppSession"  // Nom du fichier SharedPreferences
    private const val KEY_IS_LOGGED_IN = "isLoggedIn"  // Clé pour vérifier si l'utilisateur est connecté
    private const val KEY_USER_ID = "userId"  // Clé pour stocker l'ID de l'utilisateur
    private const val KEY_USER_EMAIL = "userEmail"  // Clé pour stocker l'email de l'utilisateur

    // Fonction privée pour obtenir une instance de SharedPreferences
    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)  // Récupère les SharedPreferences en mode privé
    }

    // Fonction pour créer une session utilisateur
    fun createSession(context: Context, userId: Long, email: String) {
        val editor = getSharedPreferences(context).edit()  // Obtient un éditeur pour modifier les SharedPreferences
        editor.putBoolean(KEY_IS_LOGGED_IN, true)  // Met à jour l'état de connexion de l'utilisateur
        editor.putLong(KEY_USER_ID, userId)  // Met à jour l'ID de l'utilisateur
        editor.putString(KEY_USER_EMAIL, email)  // Met à jour l'email de l'utilisateur
        editor.apply()  // Applique les modifications
    }

    // Fonction pour supprimer la session utilisateur
    fun clearSession(context: Context) {
        val editor = getSharedPreferences(context).edit()  // Obtient un éditeur pour modifier les SharedPreferences
        editor.clear()  // Efface toutes les données de session
        editor.apply()  // Applique les modifications
    }

    // Fonction pour vérifier si l'utilisateur est connecté
    fun isLoggedIn(context: Context): Boolean {
        return getSharedPreferences(context).getBoolean(KEY_IS_LOGGED_IN, false)  // Retourne true si l'utilisateur est connecté, sinon false
    }

    // Fonction pour obtenir l'ID de l'utilisateur
    fun getUserId(context: Context): Long {
        return getSharedPreferences(context).getLong(KEY_USER_ID, -1)  // Retourne l'ID de l'utilisateur ou -1 si non défini
    }

    // Fonction pour obtenir l'email de l'utilisateur
    fun getUserEmail(context: Context): String? {
        return getSharedPreferences(context).getString(KEY_USER_EMAIL, null)  // Retourne l'email de l'utilisateur ou null si non défini
    }
}
