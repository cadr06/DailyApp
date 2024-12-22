package com.example.dailyapp.ui.tasks



import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dailyapp.data.local.AppDatabase
import com.example.dailyapp.data.model.Task
import com.example.dailyapp.data.model.TaskStatus
import com.example.dailyapp.utils.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    private val taskDao = AppDatabase.getDatabase(application).taskDao()
    private val userId = SessionManager.getUserId(application)

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks

    private val _notifiedTasks = MutableStateFlow<List<Task>>(emptyList())
    val notifiedTasks: StateFlow<List<Task>> = _notifiedTasks

    private var currentFilter = TaskStatus.PENDING

    init {
        loadTasks()
    }

    suspend fun getTask(taskId: Long, userId: Long): Task? {
        return taskDao.getTask(taskId, userId)
    }

    private fun loadTasks() {
        viewModelScope.launch {
            taskDao.getTasksForUserByStatus(userId, currentFilter).collect {
                _tasks.value = it
            }
        }
    }
    
    

    fun setFilter(status: TaskStatus) {
        currentFilter = status
        loadTasks()
    }

    fun addTask(task: Task) {
        viewModelScope.launch {
            taskDao.insertTask(task.copy(userId = userId))
        }
    }

    suspend fun markAllNotificationsAsRead() {
        taskDao.markAllNotificationsAsRead()
    }

    fun getNotifiedTasks(): Flow<List<Task>> {
        return taskDao.getNotifiedTasks()
    }


    fun updateTask(task: Task) {
        viewModelScope.launch {
            taskDao.updateTask(task)
        }
    }

     suspend fun getUnreadNotificationsCount(): Int {
        return taskDao.getUnreadNotificationsCount()
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            taskDao.deleteTask(task)
        }
    }

   

    fun updateTaskStatus(task: Task, newStatus: TaskStatus) {
        viewModelScope.launch {
            taskDao.updateTaskStatus(task.id, userId, newStatus)
        }
    }
}