package com.example.finalproject_breathe_again.ui.home

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.finalproject_breathe_again.utilities.DateUtilities
import com.google.firebase.firestore.FirebaseFirestore


class HomeViewModel : ViewModel() {

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _moneySaved = MutableLiveData<Double>()
    val moneySaved: LiveData<Double> get() = _moneySaved

    private val _smokeFreeDays = MutableLiveData<Int>()
    val smokeFreeDays: LiveData<Int> get() = _smokeFreeDays

    private val handler = Handler(Looper.getMainLooper())
    lateinit var userId: String
        private set
    var dailyCigarettes = 0
        private set
    var costPerCigarette = 2.0
        private set
    private var startDate: Long = 0

    fun loadUserData(userId: String) {
        _loading.value = true
        this.userId = userId
        val db = FirebaseFirestore.getInstance()

        // Listener to track real-time updates from Firestore
        db.collection("users").document(userId).addSnapshotListener { document, exception ->
            if (exception != null) {
                Log.e("HomeViewModel", "Error listening for updates: ${exception.message}")
                _loading.value = false
                return@addSnapshotListener
            }

            if (document != null && document.exists()) {
                startDate = document.getLong("startDate") ?: 0L
                dailyCigarettes = document.getLong("cigarettes")?.toInt() ?: 10 // Default to 10 cigarettes/day
                costPerCigarette = document.getDouble("costPerCigarette") ?: 2.0 // Default price per cigarette

                if (startDate == 0L) {
                    Log.e("HomeViewModel", "Invalid start date in Firestore, setting default value")
                    startDate = System.currentTimeMillis() // Fallback value
                }

                // Update progress based on new data
                updateProgress()
                _loading.value = false
            } else {
                Log.e("HomeViewModel", "Document does not exist")
                _loading.value = false
            }
        }
    }

    private fun updateProgress() {
        calculateSmokeFreeDays()
        calculateMoneySaved()
    }

    private fun calculateSmokeFreeDays() {
        if (startDate > 0) {
            val days = DateUtilities.calculateDaysBetween(startDate)
            _smokeFreeDays.value = days
            Log.d("HomeViewModel", "Smoke-Free Days Calculated: $days")
        } else {
            _smokeFreeDays.value = 0
            Log.e("HomeViewModel", "Failed to calculate smoke-free days: Invalid start date")
        }
    }

    private fun calculateMoneySaved() {
        val days = _smokeFreeDays.value ?: 0
        if (days > 0 && dailyCigarettes > 0) {
            val savedMoney = days * dailyCigarettes * costPerCigarette
            _moneySaved.value = savedMoney
            saveMoneyToFirestore(savedMoney)
            Log.d("HomeViewModel", "Money Saved Calculated: $savedMoney")
        } else {
            _moneySaved.value = 0.0
            Log.e("HomeViewModel", "Failed to calculate money saved: Invalid data (days=$days, dailyCigarettes=$dailyCigarettes)")
        }
    }

    private fun saveMoneyToFirestore(savedMoney: Double) {
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(userId)
            .update("moneySaved", savedMoney)
            .addOnSuccessListener {
                Log.d("HomeViewModel", "Money saved successfully written to Firestore: $savedMoney")
            }
            .addOnFailureListener { exception ->
                Log.e("HomeViewModel", "Failed to update money saved: ${exception.message}")
            }
    }

    override fun onCleared() {
        super.onCleared()
        handler.removeCallbacksAndMessages(null)
    }
}
