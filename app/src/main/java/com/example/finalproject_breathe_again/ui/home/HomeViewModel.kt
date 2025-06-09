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

        db.collection("users").document(userId).addSnapshotListener { document, exception ->
            if (exception != null) {
                Log.e("HomeViewModel", "Error: ${exception.message}")
                _loading.value = false
                return@addSnapshotListener
            }

            if (document != null && document.exists()) {
                startDate = document.getLong("startDate") ?: 0L
                dailyCigarettes = document.getLong("cigarettes")?.toInt() ?: 10
                costPerCigarette = document.getDouble("costPerCigarette") ?: 2.0

                if (startDate == 0L) {
                    startDate = System.currentTimeMillis()
                }

                updateProgress()
                _loading.value = false
            } else {
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
        } else {
            _smokeFreeDays.value = 0
        }
    }

    private fun calculateMoneySaved() {
        val days = _smokeFreeDays.value ?: 0
        if (days > 0 && dailyCigarettes > 0) {
            val savedMoney = days * dailyCigarettes * costPerCigarette
            _moneySaved.value = savedMoney
            saveMoneyToFirestore(savedMoney)
        } else {
            _moneySaved.value = 0.0
        }
    }

    private fun saveMoneyToFirestore(savedMoney: Double) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(userId)
            .update("moneySaved", savedMoney)
    }

    override fun onCleared() {
        super.onCleared()
        handler.removeCallbacksAndMessages(null)
    }
}
