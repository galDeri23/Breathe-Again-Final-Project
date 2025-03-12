package com.example.finalproject_breathe_again.ui.notifications

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.finalproject_breathe_again.databinding.FragmentNotificationsBinding
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NotificationsViewModel by viewModels()
    private lateinit var notificationAdapter: NotificationsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecyclerView()
        observeViewModel()

        // Set delete all notifications button click
        binding.imgDeleteAll.setOnClickListener {
            deleteAllNotifications()
            Toast.makeText(requireContext(), "Delete All feature coming soon!", Toast.LENGTH_SHORT).show()
        }
    }
    private fun deleteAllNotifications() {
        val db = FirebaseFirestore.getInstance()

        db.collection("notifications")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    document.reference.delete()
                }
                Log.d("Firestore", "All notifications deleted successfully")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error deleting notifications", e)
            }
    }

    private fun initRecyclerView() {
        notificationAdapter = NotificationsAdapter(mutableListOf()) { notification ->
            viewModel.deleteNotification(notification) // Delete notification from Firestore
        }
        binding.recyclerNotifications.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = notificationAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.notifications.observe(viewLifecycleOwner, Observer { notifications ->
            Log.d("NotificationsFragment", "Received notifications: ${notifications.size}")
            notifications.forEach { notification ->
                Log.d(
                    "NotificationsFragment",
                    "Notification: title=${notification.title}, description=${notification.description}, date=${notification.date}"
                )
            }

            if (!notifications.isNullOrEmpty()) {

                val sortedNotifications = notifications.sortedByDescending {
                    try {
                        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        dateFormat.parse(it.date) ?: Date(0)
                    } catch (e: Exception) {
                        Log.e("NotificationsFragment", "Error parsing date: ${e.message}")
                        Date(0)
                    }
                }

                notificationAdapter.updateNotifications(sortedNotifications)
                binding.recyclerNotifications.visibility = View.VISIBLE
            } else {
                binding.recyclerNotifications.visibility = View.GONE
            }
        })
        viewModel.fetchNotificationsInRealtime()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
