package com.example.finalproject_breathe_again.ui.home

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
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
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import android.provider.MediaStore
import androidx.navigation.NavOptions

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
                binding.tvDaysValue.text = days.toString()


                val maxDays = 365
                binding.progressCircularDays.max = maxDays


                binding.progressCircularDays.progress = days.coerceAtMost(maxDays)

                if (days >= maxDays) {
                    Toast.makeText(context, "Congratulations! You've passed your goal!", Toast.LENGTH_SHORT).show()
                }
            }

            homeViewModel.moneySaved.observe(viewLifecycleOwner) { money ->
                val moneyInt = money.toInt()
                binding.tvMoneyValue.text = "$$moneyInt"

                //change text size based on money saved
                val textSizeMoney = when {
                    moneyInt >= 10000 -> 14f
                    moneyInt >= 1000 -> 18f
                    else -> 24f
                }
                binding.tvMoneyValue.textSize = textSizeMoney

                val maxMoney = 10000
                binding.progressCircularMoney.max = maxMoney
                binding.progressCircularMoney.progress = moneyInt.coerceAtMost(maxMoney)
            }

            homeViewModel.loading.observe(viewLifecycleOwner) { isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }

            binding.btnCraving.setOnClickListener {
                findNavController().navigate(
                    R.id.navigation_craving,
                    null,
                    NavOptions.Builder()
                        .setPopUpTo(R.id.navigation_home, inclusive = false)
                        .build()
                )
            }
            binding.btnShare.setOnClickListener {
                val days = homeViewModel.smokeFreeDays.value ?: 0
                val money = homeViewModel.moneySaved.value ?: 0.0

                if (days == 0 || money == 0.0) {
                    Toast.makeText(context, "Wait until your data loads before sharing!", Toast.LENGTH_SHORT).show()
                } else {
                    showShareOptions()
                }
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
        val shareText = "I've been breathe again for $days days and saved $${String.format("%.2f", money)}! Join me in this journey!"

        val bitmap = createGraphBitmap()

        val path = MediaStore.Images.Media.insertImage(
            requireContext().contentResolver,
            bitmap,
            "Smoke-Free Progress",
            null
        )

        val uri = Uri.parse(path)

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_TEXT, shareText)
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        startActivity(Intent.createChooser(intent, "Share your progress"))
    }


    private fun createGraphBitmap(): Bitmap {
        //function to create a bitmap of the graph
        val view = binding.progressCircularDays
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    private fun shareInApp(days: Int, money: Double) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val db = FirebaseDatabase.getInstance().getReference("shareItems")

            val userRef = FirebaseFirestore.getInstance().collection("users").document(userId)

            userRef.get().addOnSuccessListener { document ->
                val username = document.getString("name") ?: "Anonymous"

                if (days > 0) {
                    val shareData = mapOf(
                        "userId" to userId,
                        "username" to username,
                        "daysFree" to days,
                        "moneySaved" to money,
                        "progressGraph" to calculateProgressPercentage(days),
                        "timestamp" to System.currentTimeMillis()
                    )

                    db.push().setValue(shareData)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Shared successfully!", Toast.LENGTH_SHORT).show()
                            findNavController().navigate(R.id.navigation_share)
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Failed to share: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(context, "No valid progress to share.", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { e ->
                Toast.makeText(context, "Failed to fetch user data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "User not logged in!", Toast.LENGTH_SHORT).show()
        }
    }


    private fun calculateProgressPercentage(days: Int): Int {
        val maxDays = binding.progressCircularDays.max
        return (days * 100) / maxDays
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
