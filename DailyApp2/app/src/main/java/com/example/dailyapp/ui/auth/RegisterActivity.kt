package com.example.dailyapp.ui.auth

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.dailyapp.data.local.AppDatabase
import com.example.dailyapp.data.model.User
import com.example.dailyapp.databinding.ActivityRegisterBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterActivity : AppCompatActivity() {
    // Liaison avec les éléments de l'interface via ViewBinding
    private lateinit var binding: ActivityRegisterBinding

    // Accès au DAO utilisateur pour interagir avec la base de données
    private val userDao by lazy { AppDatabase.getDatabase(this).userDao() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialisation du binding pour lier l'UI de l'activité
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configuration des listeners de clics
        setupClickListeners()
    }

    // Fonction pour configurer les écouteurs de clics
    private fun setupClickListeners() {
        // Clic sur le bouton d'inscription
        binding.registerButton.setOnClickListener {
            val firstName = binding.firstNameInput.text.toString().trim()
            val lastName = binding.lastNameInput.text.toString().trim()
            val email = binding.emailInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()

            if (validateInputs(firstName, lastName, email, password)) {
                // Si les champs sont valides, procéder à l'inscription
                register(firstName, lastName, email, password)
            }
        }

        // Clic sur le lien pour retourner à la page de connexion
        binding.loginLink.setOnClickListener {
            finish()  // Ferme l'activité actuelle et retourne à la page précédente
        }
    }

    // Fonction pour valider les entrées utilisateur (prénom, nom, email, mot de passe)
    private fun validateInputs(firstName: String, lastName: String, email: String, password: String): Boolean {
        var isValid = true

        // Vérification du prénom
        if (firstName.isEmpty()) {
            binding.firstNameLayout.error = "Le prénom est requis"
            isValid = false
        } else {
            binding.firstNameLayout.error = null
        }

        // Vérification du nom
        if (lastName.isEmpty()) {
            binding.lastNameLayout.error = "Le nom est requis"
            isValid = false
        } else {
            binding.lastNameLayout.error = null
        }

        // Vérification de l'email
        if (email.isEmpty()) {
            binding.emailLayout.error = "L'email est requis"
            isValid = false
        } else if (!isValidEmailFormat(email)) {
            binding.emailLayout.error = "L'email n'est pas valide"
            isValid = false
        } else {
            binding.emailLayout.error = null
        }

        // Vérification du mot de passe
        if (password.isEmpty()) {
            binding.passwordLayout.error = "Le mot de passe est requis"
            isValid = false
        } else if (password.length < 6) {
            binding.passwordLayout.error = "Le mot de passe doit contenir au moins 6 caractères"
            isValid = false
        } else {
            binding.passwordLayout.error = null
        }

        return isValid
    }

    // Fonction pour valider le format de l'email avec une expression régulière
    private fun isValidEmailFormat(email: String): Boolean {
        // Regex pour valider un format d'email standard
        val emailRegex = Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")
        return emailRegex.matches(email)
    }

    // Fonction d'inscription
    private fun register(firstName: String, lastName: String, email: String, password: String) {
        // Utilisation des coroutines pour effectuer les opérations sur la base de données
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Vérification si l'email existe déjà dans la base de données
                val emailExists = userDao.isEmailExists(email)
                if (emailExists) {
                    // Si l'email existe, afficher un message d'erreur
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@RegisterActivity, "Cet email existe déjà", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                // Création d'un nouvel utilisateur
                val user = User(
                    firstName = firstName,
                    lastName = lastName,
                    email = email,
                    password = password
                )

                // Insertion du nouvel utilisateur dans la base de données
                userDao.insertUser(user)

                // Affichage d'un message de succès
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@RegisterActivity, "Inscription réussie", Toast.LENGTH_SHORT).show()
                    finish()  // Ferme l'activité et retourne à la page de connexion
                }
            } catch (e: Exception) {
                // En cas d'erreur, afficher un message d'erreur
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@RegisterActivity, "Erreur lors de l'inscription : ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
