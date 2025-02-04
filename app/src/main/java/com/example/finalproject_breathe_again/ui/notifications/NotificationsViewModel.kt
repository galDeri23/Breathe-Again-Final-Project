package com.example.finalproject_breathe_again.ui.notifications

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.finalproject_breathe_again.ui.notifications.Notification
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class NotificationsViewModel : ViewModel() {

    private val _notifications = MutableLiveData<List<Notification>>(emptyList())
    val notifications: LiveData<List<Notification>> = _notifications

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    init {
        loadNotifications()
    }

    private fun loadNotifications() {
        _loading.value = true

        // גישה ל-Firestore
        val firestore = FirebaseFirestore.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser

        // בדיקה אם המשתמש מחובר
        if (currentUser == null) {
            _errorMessage.postValue("User is not logged in.")
            _loading.postValue(false)
            return
        }

        firestore.collection("notifications")
            .whereEqualTo("userId", currentUser.uid)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val notifications = querySnapshot.documents.mapNotNull { document ->
                    document.toObject(Notification::class.java)
                }

                _notifications.postValue(notifications)
                _loading.postValue(false)
            }
            .addOnFailureListener { exception ->
                _errorMessage.postValue("Failed to load notifications: ${exception.message}")
                _loading.postValue(false)
            }
    }

    fun deleteNotification(notification: Notification) {
        val firestore = FirebaseFirestore.getInstance()

        // Delete notification from Firestore
        firestore.collection("notifications")
            .document(notification.id)
            .delete()
            .addOnSuccessListener {
                // Update local list in ViewModel
                val updatedList = _notifications.value?.toMutableList()?.apply {
                    remove(notification)
                } ?: emptyList()
                _notifications.postValue(updatedList)
            }
            .addOnFailureListener { exception ->
                Log.e("NotificationsViewModel", "Error deleting notification: ${exception.message}")
            }
    }
}
