package com.example.dailyapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.dailyapp.data.local.AppDatabase
import com.example.dailyapp.databinding.ActivityLoginBinding
import com.example.dailyapp.ui.MainActivity
import com.example.dailyapp.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {
    // Binding pour lier les vues avec les éléments de l'interface utilisateur
    private lateinit var binding: ActivityLoginBinding

    // Accès au DAO utilisateur pour interagir avec la base de données
    private val userDao by lazy { AppDatabase.getDatabase(this).userDao() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialisation du binding
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialisation des listeners de clics
        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Gérer le clic sur le bouton de connexion
        binding.loginButton.setOnClickListener {
            val email = binding.emailInput.text.toString()
            val password = binding.passwordInput.text.toString()

            if (validateInputs(email, password)) {
                // Si les entrées sont valides, procéder à la connexion
                login(email, password)
            }
        }

        // Lien pour accéder à l'écran d'inscription
        binding.registerLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    // Fonction pour valider les champs email et mot de passe
    private fun validateInputs(email: String, password: String): Boolean {
        // Vérification que l'email n'est pas vide
        if (email.isEmpty()) {
            binding.emailLayout.error = "L'email est requis"
            return false
        }
        // Vérification que le mot de passe n'est pas vide
        if (password.isEmpty()) {
            binding.passwordLayout.error = "Le mot de passe est requis"
            return false
        }
        return true
    }

    // Fonction pour effectuer la connexion
    private fun login(email: String, password: String) {
        // Utilisation des coroutines pour effectuer la requête dans un thread d'arrière-plan
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Tentative de récupération de l'utilisateur dans la base de données
                val user = userDao.getUser(email, password)
                withContext(Dispatchers.Main) {
                    // Si un utilisateur est trouvé, connexion réussie
                    if (user != null) {
                        // Sauvegarde de la session utilisateur
                        SessionManager.createSession(this@LoginActivity, user.id, user.email)
                        Toast.makeText(this@LoginActivity, "Connexion réussie", Toast.LENGTH_SHORT).show()

                        // Redirection vers l'activité principale
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finishAffinity() // Ferme toutes les activités précédentes
                    } else {
                        // Si l'utilisateur n'est pas trouvé, afficher un message d'erreur
                        Toast.makeText(this@LoginActivity, "Email ou mot de passe incorrect", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                // En cas d'exception, afficher un message d'erreur
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@LoginActivity, "Erreur lors de la connexion", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
