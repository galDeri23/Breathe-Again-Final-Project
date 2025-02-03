package com.example.finalproject_breathe_again.ui.home

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit

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

        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {

                    startDate = document.getLong("startDate") ?: 0L
                    dailyCigarettes = document.getLong("cigarettes")?.toInt() ?: 0

                    if (startDate == 0L) {
                        Log.e("HomeViewModel", "Invalid start date in Firestore")
                    } else {
                        calculateSmokeFreeDays()
                        calculateMoneySaved()
                    }

                    _loading.value = false
                } else {
                    Log.e("HomeViewModel", "Document does not exist in Firestore")
                    _loading.value = false
                }
            }
            .addOnFailureListener { exception ->
                Log.e("HomeViewModel", "Error loading user data: ${exception.message}")
                _loading.value = false
            }
    }

    private fun calculateSmokeFreeDays() {
        if (startDate > 0) {
            val days = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - startDate).toInt()
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
            Log.e("HomeViewModel", "Failed to calculate money saved: Invalid data")
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
