package com.example.finalproject_breathe_again.utilities

import android.icu.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object DateUtilities {
    fun getCurrentDateAsString(): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return dateFormat.format(Date())
    }
    fun calculateDaysBetween(startDate: Long): Int {
        val currentTime = System.currentTimeMillis()
        return TimeUnit.MILLISECONDS.toDays(currentTime - startDate).toInt()
    }
}