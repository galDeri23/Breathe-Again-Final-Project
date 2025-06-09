package com.example.finalproject_breathe_again.ui.notifications

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.finalproject_breathe_again.utilities.DateUtilities
import com.example.finalproject_breathe_again.utilities.NotificationUtilities
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class NotificationWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val firestore = FirebaseFirestore.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser == null) {
            Log.e("NotificationWorker", "User is not logged in.")
            return Result.failure()
        }

        NotificationUtilities.fetchUserDocument(
            firestore = firestore,
            userId = currentUser.uid,
            onSuccess = { document ->
                if (document.exists()) {
                    val startDate = document.getLong("startDate")
                    if (startDate != null) {
                        val daysFree = DateUtilities.calculateDaysBetween(startDate)

                        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                        val notification = when (hour) {
                            in 6..9 -> Notification(
                                id = "",
                                title = "Good Morning ☀️",
                                description = "A new day is a new opportunity – keep going smoke-free!",
                                date = DateUtilities.getCurrentDateAsString(),
                                userId = currentUser.uid
                            )

                            in 10..11, in 15..16 -> Notification(
                                id = "",
                                title = "Need a break?",
                                description = "Feeling a craving? Let's handle this together! Try a breathing exercise or motivational quote.",
                                date = DateUtilities.getCurrentDateAsString(),
                                userId = currentUser.uid
                            )

                            in 12..14 -> Notification(
                                id = "",
                                title = "Set your daily goal 🎯",
                                description = "Choose a small goal for today, like drinking water instead of smoking or practicing calm breathing.",
                                date = DateUtilities.getCurrentDateAsString(),
                                userId = currentUser.uid
                            )

                            in 21..23 -> Notification(
                                id = "",
                                title = "Daily Summary 📊",
                                description = "Great job! You've been smoke-free for $daysFree days. Reflect on how you handled today 💪",
                                date = DateUtilities.getCurrentDateAsString(),
                                userId = currentUser.uid
                            )

                            else -> Notification(
                                id = "",
                                title = "Keep going!",
                                description = "Every smoke-free moment makes you stronger. Proud of you! 💙",
                                date = DateUtilities.getCurrentDateAsString(),
                                userId = currentUser.uid
                            )
                        }

                        NotificationUtilities.sendNotification(
                            firestore = firestore,
                            notification = notification,
                            onSuccess = {
                                android.os.Handler(android.os.Looper.getMainLooper()).post {
                                    android.widget.Toast.makeText(
                                        applicationContext,
                                        "You have a new message in the Achievements screen 📩",
                                        android.widget.Toast.LENGTH_LONG
                                    ).show()
                                }
                            },
                            onFailure = {
                                Log.e("NotificationWorker", "Failed to send notification: ${it.message}")
                            }
                        )
                    }
                }
            },
            onFailure = {
                Log.e("NotificationWorker", "Failed to fetch user document: ${it.message}")
            }
        )

        return Result.success()
    }
}
