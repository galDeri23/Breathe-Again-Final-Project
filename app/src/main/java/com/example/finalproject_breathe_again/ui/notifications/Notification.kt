package com.example.finalproject_breathe_again.ui.notifications

import com.example.finalproject_breathe_again.utilities.DateUtilities
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Notification(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val date: String = "",
    val userId: String = "")

