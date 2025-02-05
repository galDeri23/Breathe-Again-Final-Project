package com.example.finalproject_breathe_again.ui.notifications

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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

        val firestore = FirebaseFirestore.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser

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
                    document.toObject(Notification::class.java)?.copy(
                        id = document.id,
                        date = document.getString("date") ?: "Unknown date" // שימוש בתאריך מהמסמך
                    )
                }
                _notifications.postValue(notifications)
                _loading.postValue(false)
            }
            .addOnFailureListener { exception ->
                _errorMessage.postValue("Failed to load notifications: ${exception.message}")
                Log.e("NotificationsViewModel", "Error: ${exception.message}")
                _loading.postValue(false)
            }

    }
    fun fetchNotificationsInRealtime() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance()
            .collection("notifications")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _errorMessage.value = "Failed to listen to notifications: ${error.message}"
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val notifications = snapshot.toObjects(Notification::class.java)
                    _notifications.value = notifications
                } else {
                    _notifications.value = emptyList()
                }
            }
    }
    fun deleteNotification(notification: Notification) {
        val firestore = FirebaseFirestore.getInstance()

        firestore.collection("notifications")
            .document(notification.id)
            .delete()
            .addOnSuccessListener {
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
