package com.example.finalproject_breathe_again.ui.home

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.finalproject_breathe_again.R
import com.example.finalproject_breathe_again.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
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
                binding.tvDaysFree.text = "Smoke-free for $days days"

                val months = days / 30
                val daysToNextMedal = 30 - (days % 30)

                showMedals(months)

                binding.tvNextGoal.text = if (days % 30 == 0 && days > 0) {
                    "🎉 You just earned a new medal!"
                } else {
                    "Next medal in $daysToNextMedal days"
                }
            }

            // גם אם לא מוצגים, נשמר בשביל שיתוף
            homeViewModel.moneySaved.observe(viewLifecycleOwner) {}

            homeViewModel.loading.observe(viewLifecycleOwner) { isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }

            binding.btnCraving.setOnClickListener {
                findNavController().navigate(R.id.breathingFragment)
            }

            binding.fabShare.setOnClickListener {
                val days = homeViewModel.smokeFreeDays.value ?: 0
                val money = homeViewModel.moneySaved.value ?: 0.0

                if (days == 0 || money == 0.0) {
                    Toast.makeText(context, "Wait until your data loads before sharing!", Toast.LENGTH_SHORT).show()
                } else {
                    showShareOptions()
                }
            }
        }
    }

    private fun showMedals(monthsFree: Int) {
        val medalContainer = binding.medalContainer
        medalContainer.removeAllViews()

        for (i in 1..monthsFree.coerceAtMost(12)) {
            val imageView = ImageView(requireContext())
            imageView.setImageResource(R.drawable.medal)

            val sizePx = (48 * resources.displayMetrics.density).toInt()
            val params = LinearLayout.LayoutParams(sizePx, sizePx)
            params.setMargins(8, 0, 8, 0)
            imageView.layoutParams = params

            medalContainer.addView(imageView)
        }
    }

    override fun onResume() {
        super.onResume()
        FirebaseAuth.getInstance().currentUser?.uid?.let {
            homeViewModel.loadUserData(it)
        }
    }

    private fun showShareOptions() {
        val days = homeViewModel.smokeFreeDays.value ?: 0
        val money = homeViewModel.moneySaved.value ?: 0.0

        val options = arrayOf("Share on Social Media", "Share in App")
        AlertDialog.Builder(requireContext())
            .setTitle("Choose Sharing Option")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> shareOnSocialMedia(days)
                    1 -> shareInApp(days)
                }
            }
            .show()
    }

    private fun shareOnSocialMedia(days: Int) {
        val shareText = "I've been smoke-free for $days days! 🎖️ Join me on my journey!"

        val bitmap = createMedalsBitmap()

        val path = MediaStore.Images.Media.insertImage(
            requireContext().contentResolver,
            bitmap,
            "My Smoke-Free Medals",
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

    private fun createMedalsBitmap(): Bitmap {
        val view = binding.medalContainer
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    private fun shareInApp(days: Int) {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        val userId = currentUser.uid
        val db = FirebaseDatabase.getInstance().getReference("shareItems")

        val userRef = FirebaseFirestore.getInstance().collection("users").document(userId)

        userRef.get().addOnSuccessListener { document ->
            val username = document.getString("name") ?: "Anonymous"

            val medals = days / 30
            val shareData = mapOf(
                "userId" to userId,
                "username" to username,
                "daysFree" to days,
                "medalsEarned" to medals,
                "message" to "I've earned $medals medal(s) for being smoke-free for $days days! 🎖️",
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

        }.addOnFailureListener { e ->
            Toast.makeText(context, "Failed to fetch user data: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
