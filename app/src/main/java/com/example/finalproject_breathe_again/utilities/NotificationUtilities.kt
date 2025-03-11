package com.example.finalproject_breathe_again.utilities

import com.example.finalproject_breathe_again.ui.notifications.Notification
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

object NotificationUtilities {
    fun fetchUserDocument(
        firestore: FirebaseFirestore,
        userId: String,
        onSuccess: (document: DocumentSnapshot) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        firestore.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener(onSuccess)
            .addOnFailureListener(onFailure)
    }

    fun sendNotification(
        firestore: FirebaseFirestore,
        notification: Notification,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val documentReference = firestore.collection("notifications").document()
        val notificationWithId = notification.copy(id = documentReference.id)

        documentReference.set(notificationWithId)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener(onFailure)
    }
}