package com.example.finalproject_breathe_again.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.finalproject_breathe_again.R
import com.example.finalproject_breathe_again.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            homeViewModel.loadUserData(userId)

            homeViewModel.smokeFreeDays.observe(viewLifecycleOwner) { days ->
                binding.tvDaysValue.text = "$days"
                val maxDays = binding.progressCircularDays.max
                binding.progressCircularDays.progress = minOf(days, maxDays)
            }

            homeViewModel.moneySaved.observe(viewLifecycleOwner) { money ->
                "$${String.format("%.2f", money)}".also { binding.tvMoneyValue.text = it }
                val maxMoney = binding.progressCircularMoney.max
                binding.progressCircularMoney.progress = minOf(money.toInt(), maxMoney)
            }


            homeViewModel.loading.observe(viewLifecycleOwner) { isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }

            binding.btnCraving.setOnClickListener {
                findNavController().navigate(R.id.navigation_craving)
            }
            binding.btnShare.setOnClickListener {
                showShareOptions()
            }
            binding.imgReset.setOnClickListener {
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser != null) {
                    val userId = currentUser.uid
                    val db = FirebaseFirestore.getInstance()


                    val resetData = mapOf(
                        "startDate" to System.currentTimeMillis(),
                        "moneySaved" to 0.0
                    )

                    db.collection("users").document(userId).update(resetData)
                        .addOnSuccessListener {

                            homeViewModel.loadUserData(userId)
                            Toast.makeText(context, "Data reset successfully!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Failed to reset data: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(context, "User not logged in!", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(context, "User not logged in!", Toast.LENGTH_SHORT).show()
            //  findNavController().navigate(R.id.action_home_to_login)
        }
    }

    override fun onResume() {
        super.onResume()
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            homeViewModel.loadUserData(currentUser.uid)
        }
    }

    private fun showShareOptions() {
        val days = homeViewModel.smokeFreeDays.value ?: 0
        val money = homeViewModel.moneySaved.value ?: 0.0

        val options = arrayOf("Share on Social Media", "Share in App")
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Choose Sharing Option")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> shareOnSocialMedia(days, money)
                1 -> shareInApp(days, money)
            }
        }
        builder.show()
    }

    private fun shareOnSocialMedia(days: Int, money: Double) {
        val shareText = "I've been breathe again for $days days and saved $$money so far! Join me in this journey!"
        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_TEXT, shareText)
        }
        startActivity(android.content.Intent.createChooser(intent, "Share your progress"))
    }

    private fun shareInApp(days: Int, money: Double) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val db = FirebaseFirestore.getInstance()

            val shareData = hashMapOf(
                "userId" to userId,
                "daysFree" to days,
                "moneySaved" to money,
                "timestamp" to System.currentTimeMillis()
            )

            db.collection("shares").add(shareData)
                .addOnSuccessListener {
                    Toast.makeText(context, "Shared successfully in the app!", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.navigation_share)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Failed to share in the app: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(context, "User not logged in!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
