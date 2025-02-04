package com.example.finalproject_breathe_again.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.finalproject_breathe_again.databinding.FragmentNotificationsBinding

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
            Toast.makeText(requireContext(), "Delete All feature coming soon!", Toast.LENGTH_SHORT).show()
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
        // Observe the list of notifications
        viewModel.notifications.observe(viewLifecycleOwner, Observer { notifications ->
            if (notifications.isNotEmpty()) {
                notificationAdapter.updateNotifications(notifications)
                binding.recyclerNotifications.visibility = View.VISIBLE
            } else {
                Toast.makeText(requireContext(), "No notifications to display", Toast.LENGTH_SHORT).show()
            }
        })

        // Observe loading state
        viewModel.loading.observe(viewLifecycleOwner, Observer { isLoading ->
            binding.recyclerNotifications.visibility = if (isLoading) View.GONE else View.VISIBLE
        })

        // Observe error messages
        viewModel.errorMessage.observe(viewLifecycleOwner, Observer { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
