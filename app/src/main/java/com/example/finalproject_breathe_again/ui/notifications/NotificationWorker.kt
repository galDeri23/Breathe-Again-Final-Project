package com.example.finalproject_breathe_again.ui.notifications

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.finalproject_breathe_again.utilities.DateUtilities
import com.example.finalproject_breathe_again.utilities.NotificationUtilities
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore



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
                        val predefinedNotifications = listOf(
                            Notification(
                                id = "",
                                title = "Keep Going!",
                                description = "Well done, you've been breathing again for $daysFree days!",
                                date = DateUtilities.getCurrentDateAsString(),
                                userId = currentUser.uid
                            ),
                            Notification(
                                id = "",
                                title = "Reminder",
                                description = "Remember to breathe deeply and stay calm!",
                                date = DateUtilities.getCurrentDateAsString(),
                                userId = currentUser.uid
                            ),
                            Notification(
                                id = "",
                                title = "Motivation",
                                description = "You're stronger than the cravings. Keep it up!",
                                date = DateUtilities.getCurrentDateAsString(),
                                userId = currentUser.uid
                            )
                        )

                        val randomNotification = predefinedNotifications.random()

                        NotificationUtilities.sendNotification(
                            firestore = firestore,
                            notification = randomNotification,
                            onSuccess = {
                                android.os.Handler(android.os.Looper.getMainLooper()).post {
                                    android.widget.Toast.makeText(
                                        applicationContext,
                                        "A new message is waiting for you in the notifications screen",
                                        android.widget.Toast.LENGTH_LONG
                                    ).show()
                                }
                            },
                            onFailure = {
                                Log.e("NotificationWorker", "Error adding notification: ${it.message}")
                            }
                        )
                    }
                }
            },
            onFailure = {
                Log.e("NotificationWorker", "Error fetching user document: ${it.message}")
            }
        )

        return Result.success()
    }
}
