package com.example.dailyapp.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.dailyapp.ui.auth.LoginActivity
import com.example.dailyapp.utils.SessionManager
import android.util.Log

class SplashActivity : AppCompatActivity() {
    private val TAG = "SplashActivity"  // Tag pour le logging, utilisé pour identifier les logs spécifiques à cette activité

    // Méthode appelée lors de la création de l'activité
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Starting SplashActivity")  // Log pour indiquer que l'activité Splash est lancée

        try {
            // Vérifier si l'utilisateur est connecté en utilisant SessionManager
            if (SessionManager.isLoggedIn(this)) {
                // Si l'utilisateur est connecté, on le redirige vers MainActivity
                Log.d(TAG, "User is logged in, redirecting to MainActivity")
                startActivity(Intent(this, MainActivity::class.java))  // Démarrer MainActivity
            } else {
                // Si l'utilisateur n'est pas connecté, on le redirige vers LoginActivity
                Log.d(TAG, "User is not logged in, redirecting to LoginActivity")
                startActivity(Intent(this, LoginActivity::class.java))  // Démarrer LoginActivity
            }
        } catch (e: Exception) {
            // Si une erreur survient, on logue l'exception
            Log.e(TAG, "Error in SplashActivity: ", e)
        }

        finish() // Fermer SplashActivity pour empêcher l'utilisateur de revenir en arrière
    }
}
