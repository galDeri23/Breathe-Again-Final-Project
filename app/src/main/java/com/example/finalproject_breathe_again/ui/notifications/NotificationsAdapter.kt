package com.example.finalproject_breathe_again.ui.notifications

import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.finalproject_breathe_again.R
import java.util.Date
import java.util.Locale

class NotificationsAdapter(
    private val notificationsList: MutableList<Notification>,
    private val onDeleteClick: (Notification) -> Unit
) : RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder>() {

    // ViewHolder
    inner class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardNotification: CardView = itemView.findViewById(R.id.card_notification)
        val tvNotificationTitle: TextView = itemView.findViewById(R.id.tv_notification_title)
        val tvNotificationDescription: TextView = itemView.findViewById(R.id.tv_notification_description)
        val tvNotificationDate: TextView = itemView.findViewById(R.id.tv_notification_date)
        val imgDeleteNotification: ImageView = itemView.findViewById(R.id.img_delete_notification)

        var isExpanded = false
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notifications, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notificationsList[position]
        Log.d("NotificationsAdapter", "Binding notification at position $position: title=${notification.title}, description=${notification.description}, date=${notification.date}")

        // Set data with formatted date
        holder.tvNotificationTitle.text = notification.title
        holder.tvNotificationDescription.text = notification.description
        holder.tvNotificationDate.text = formatDate(notification.date)

        // Handle delete button click
        holder.imgDeleteNotification.setOnClickListener {
            onDeleteClick(notification)
        }

        // Expand/Collapse card on click
        holder.cardNotification.setOnClickListener {
            val isExpanded = holder.tvNotificationDescription.maxLines == Integer.MAX_VALUE

            if (isExpanded) {
                // Collapse the card
                holder.tvNotificationDescription.maxLines = 2
                holder.cardNotification.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            } else {
                // Expand the card
                holder.tvNotificationDescription.maxLines = Integer.MAX_VALUE
                holder.cardNotification.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            }

            holder.cardNotification.requestLayout() // Refresh layout
        }
    }


    private fun formatDate(date: String): String {
        return when (date.lowercase(Locale.getDefault())) {
            "today" -> {
                val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                formatter.format(Date())
            }
            "yesterday" -> {
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.DAY_OF_MONTH, -1)
                val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                formatter.format(calendar.time)
            }
            else -> date
        }
    }

    override fun getItemCount(): Int = notificationsList.size

    // Helper function to update notifications
    fun updateNotifications(newList: List<Notification>) {
        notificationsList.clear()
        notificationsList.addAll(newList)
        Log.d("NotificationsAdapter", "Updated notifications: ${notificationsList.size}")
        notifyDataSetChanged()
    }

}
