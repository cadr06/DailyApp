package com.example.dailyapp.ui.tasks

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.dailyapp.data.model.Task
import com.example.dailyapp.data.model.TaskStatus
import com.example.dailyapp.databinding.ActivityAddEditTaskBinding
import com.example.dailyapp.utils.SessionManager
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddEditTaskActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddEditTaskBinding
    private val viewModel: TaskViewModel by viewModels()
    private var selectedDate: Date? = null
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val userId: Long by lazy { SessionManager.getUserId(this) }


    private var taskToEdit: Task? = null
    private var isEditMode = false


    private lateinit var placesClient: PlacesClient

    private var selectedLatLng: LatLng? = null
    private var selectedAddress: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        viewModel = ViewModelProvider(this)[TaskViewModel::class.java]
        // Récupérer l'ID de la tâche si on est en mode édition
        val taskId = intent.getLongExtra("task_id", -1)
        if (taskId != -1L) {
            isEditMode = true
            loadTask(taskId)
        }

        Places.initialize(applicationContext, "AIzaSyAHJZeqgUWtwu72JrSvDooACbv-Gl-lW7o")
        placesClient = Places.createClient(this)

        setupToolbar()
        setupDatePicker()
        setupSaveButton()
        setupLocationPicker()

    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Nouvelle tâche"
    }

    private fun setupLocationPicker() {
        binding.selectLocationButton.setOnClickListener {
            // Créer l'intent pour le sélecteur de lieu
            val fields = listOf(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.ADDRESS,
                Place.Field.LAT_LNG
            )

            val intent = Autocomplete.IntentBuilder(
                AutocompleteActivityMode.FULLSCREEN, fields)
                .build(this)
            
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    data?.let {
                        val place = Autocomplete.getPlaceFromIntent(data)
                        binding.addressInput.setText(place.address)
                        selectedAddress = place.address
                        selectedLatLng = place.latLng
                    }
                }
                AutocompleteActivity.RESULT_ERROR -> {
                    data?.let {
                        val status = Autocomplete.getStatusFromIntent(data)
                        Toast.makeText(this, "Erreur : ${status.statusMessage}", Toast.LENGTH_SHORT).show()
                    }
                }
                Activity.RESULT_CANCELED -> {
                    // L'utilisateur a annulé la sélection
                }
            }
        }
    }

    companion object {
        private const val AUTOCOMPLETE_REQUEST_CODE = 1
    }



    private fun loadTask(taskId: Long) {
        lifecycleScope.launch {
            val userId = SessionManager.getUserId(this@AddEditTaskActivity)
            taskToEdit = viewModel.getTask(taskId, userId)
            taskToEdit?.let { task ->
                // Remplir les champs avec les données de la tâche
                binding.titleInput.setText(task.title)
                binding.descriptionInput.setText(task.description)
                selectedDate = task.dueDate
                binding.dueDateInput.setText(dateFormat.format(task.dueDate))
                binding.addressInput.setText(task.address ?: "")
                selectedLatLng = if (task.latitude != null && task.longitude != null) {
                    LatLng(task.latitude, task.longitude)
                } else null
                selectedAddress = task.address
            }
        }
    }
  private fun setupDatePicker() {
    binding.dueDateInput.setOnClickListener {
        val calendar = Calendar.getInstance()
        selectedDate?.let {
            calendar.time = it
        }

        val todayCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Date Picker
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, day ->
                calendar.set(year, month, day)
                // Après avoir sélectionné la date, on ouvre le Time Picker
                TimePickerDialog(
                    this,
                    { _, hour, minute ->
                        calendar.set(Calendar.HOUR_OF_DAY, hour)
                        calendar.set(Calendar.MINUTE, minute)
                        selectedDate = calendar.time
                        // Format pour afficher date et heure
                        val dateTimeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                        binding.dueDateInput.setText(dateTimeFormat.format(selectedDate!!))
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true // Format 24h
                ).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.datePicker.minDate = todayCalendar.timeInMillis
        datePickerDialog.show()
    }
}

    private fun setupSaveButton() {
        binding.saveButton.setOnClickListener {
            if (validateInputs()) {


                if (isEditMode ) {
                    val task = Task(
                        id = taskToEdit!!.id,
                        userId = userId,
                        title = binding.titleInput.text.toString(),
                        description = binding.descriptionInput.text.toString(),
                        dueDate = selectedDate!!,
                        status = taskToEdit!!.status,
                        address = selectedAddress ?: taskToEdit!!.address,
                       latitude = selectedLatLng?.latitude ?: taskToEdit!!.latitude,
                       longitude = selectedLatLng?.longitude ?: taskToEdit!!.longitude
                    )
                    viewModel.updateTask(task)
                    Toast.makeText(this, "Tâche modifiée avec succès", Toast.LENGTH_SHORT).show()
                } else {
                    val task = Task(
                        userId = userId,
                        title = binding.titleInput.text.toString(),
                        description = binding.descriptionInput.text.toString(),
                        dueDate = selectedDate!!,
                        status = TaskStatus.PENDING,
                        address = selectedAddress ?: binding.addressInput.text.toString(),
                       latitude = selectedLatLng?.latitude,
                       longitude = selectedLatLng?.longitude
                    )

                    viewModel.addTask(task)
                    Toast.makeText(this, "Tâche ajoutée avec succès", Toast.LENGTH_SHORT).show()
                }
                

                finish()
            }
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        if (binding.titleInput.text.isNullOrBlank()) {
            binding.titleLayout.error = "Le titre est requis"
            isValid = false
        }

        if (binding.descriptionInput.text.isNullOrBlank()) {
            binding.descriptionLayout.error = "La description est requise"
            isValid = false
        }

        if (selectedDate == null) {
            binding.dueDateLayout.error = "La date limite est requise"
            isValid = false
        }

        return isValid
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}