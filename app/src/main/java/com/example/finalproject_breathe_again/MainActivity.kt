package com.example.finalproject_breathe_again

import com.example.finalproject_breathe_again.utilities.NotificationUtilities
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.work.*
import com.example.finalproject_breathe_again.databinding.ActivityMainBinding
import com.example.finalproject_breathe_again.ui.notifications.Notification
import com.example.finalproject_breathe_again.ui.notifications.NotificationWorker
import com.example.finalproject_breathe_again.utilities.DateUtilities
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            redirectToLogin()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController = navHostFragment.navController

        navView.setupWithNavController(navController)
        setupActionBarWithNavController(navController)


        scheduleNotificationWorker()
        checkAndSendWelcomeNotification()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                logoutUser()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun logoutUser() {
        val sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)
        sharedPreferences.edit()
            .putLong("last_logout_time", System.currentTimeMillis())
            .apply() // Save the last logout time

        FirebaseAuth.getInstance().signOut()
        WorkManager.getInstance(this).cancelUniqueWork("NotificationWorker")

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun redirectToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
    private fun checkAndSendWelcomeNotification() {
        val sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)
        val firestore = FirebaseFirestore.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            NotificationUtilities.fetchUserDocument(
                firestore = firestore,
                userId = currentUser.uid,
                onSuccess = { document ->
                    if (document.exists()) {
                        val startDate = document.getLong("startDate")
                        val isFirstLogin = startDate != null && isToday(startDate)

                        val notification = Notification(
                            id = "",
                            title = if (isFirstLogin) "Welcome!" else "Welcome Back!",
                            description = if (isFirstLogin) {
                                "We're so glad you're here – let's begin the journey to freedom 🚀"
                            } else {
                                "Glad to see you again! Let's keep going together 💙"
                            },
                            date = DateUtilities.getCurrentDateAsString(),
                            userId = currentUser.uid
                        )

                        NotificationUtilities.sendNotification(
                            firestore = firestore,
                            notification = notification,
                            onSuccess = {
                                android.widget.Toast.makeText(
                                    applicationContext,
                                    "You have a new message in the Achievements screen",
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                            },
                            onFailure = {
                                Log.e("MainActivity", "Failed to send notification: ${it.message}")
                            }
                        )
                    }
                },
                onFailure = {
                    Log.e("MainActivity", "Error fetching user document: ${it.message}")
                }
            )
        }
    }

    private fun isToday(timestamp: Long): Boolean {
        val calendar = Calendar.getInstance()
        val today = Calendar.getInstance()
        calendar.timeInMillis = timestamp

        return calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
    }

    private fun scheduleNotificationWorker() {
        val currentTime = System.currentTimeMillis()
        val calendar = Calendar.getInstance().apply {
            timeInMillis = currentTime
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        if (calendar.timeInMillis <= currentTime) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        val initialDelay = calendar.timeInMillis - currentTime

        val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "NotificationWorker",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
