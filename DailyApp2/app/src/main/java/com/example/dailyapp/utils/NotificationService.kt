package com.example.dailyapp.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.dailyapp.R
import com.example.dailyapp.data.local.AppDatabase
import com.example.dailyapp.data.local.TaskDao
import com.example.dailyapp.data.model.Task
import com.example.dailyapp.ui.tasks.TaskDetailActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class NotificationService : Service() {
    private var handler: Handler? = null
    private lateinit var taskDao: TaskDao
    private lateinit var taskRunnable: Runnable

    private suspend fun updateNotificationCount() {
        val unreadCount = taskDao.getUnreadNotificationsCount()
        withContext(Dispatchers.Main) {
//            showNotification(task)
            val intent = Intent(ACTION_UPDATE_NOTIFICATION_COUNT)
            intent.putExtra(EXTRA_COUNT, unreadCount)
            sendBroadcast(intent)
            Log.d("NotificationService", "Broadcasting count: $unreadCount")
        }
    }

    override fun onCreate() {
        super.onCreate()
        handler = Handler(Looper.getMainLooper())
        taskDao = AppDatabase.getDatabase(this).taskDao()

        createNotificationChannel()

        taskRunnable = Runnable {
            checkUpcomingTasks()

            handler?.postDelayed(taskRunnable, 60000)
        }
    }



    private fun checkUpcomingTasks() {
        val currentTime = System.currentTimeMillis()
        val thirtyMinutesFromNow = currentTime + (30 * 60 * 1000)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val upcomingTasks = taskDao.getUpcomingTasks(Date(currentTime), Date(thirtyMinutesFromNow))

                for (task in upcomingTasks) {
                    if (!task.notified) {
                        taskDao.updateTaskNotificationStatus(task.id, true, notificationSent = true)
                        updateNotificationCount()

                        withContext(Dispatchers.Main) {

                            showNotification(task)
                        }
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }



    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "task_notifications",
                "Rappels de tâches",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications pour les tâches à venir"
                enableLights(true)
                enableVibration(true)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }



    private fun showNotification(task: Task) {
        val channelId = "task_notifications"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Créer le canal de notification pour Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Rappels de tâches",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, TaskDetailActivity::class.java).apply {
            putExtra("task_id", task.id)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            task.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Rappel : ${task.title}")
            .setContentText("Cette tâche doit être effectuée bientôt")
            .setSmallIcon(R.drawable.ic_notifications)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("${task.description}\nDate limite : ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(task.dueDate)}"))
            .build()

        notificationManager.notify(task.id.toInt(), notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        handler?.post(taskRunnable)
        return START_STICKY
    }

    companion object {
        const val ACTION_UPDATE_NOTIFICATION_COUNT = "update_notification_count"
        const val EXTRA_COUNT = "notification_count"
    }

    override fun onDestroy() {
        super.onDestroy()
        handler?.removeCallbacks(taskRunnable)
    }



}