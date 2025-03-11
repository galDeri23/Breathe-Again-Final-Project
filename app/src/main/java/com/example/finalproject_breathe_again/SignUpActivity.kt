package com.example.finalproject_breathe_again

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.finalproject_breathe_again.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        val isNewUser = intent.getBooleanExtra("isNewUser", false)
        if(!isNewUser) {
            binding.singUpName.visibility = View.VISIBLE
            binding.singUpCigarettes.visibility = View.VISIBLE
            binding.singUpStartDate.visibility = View.VISIBLE
            binding.singUpEmail.visibility = View.INVISIBLE
            binding.singUpPassword.visibility = View.INVISIBLE
            binding.singUp.setOnClickListener {
                val name = binding.singUpName.text.toString()
                val cigarettes = binding.singUpCigarettes.text.toString().toIntOrNull()
                val startDate = getDateFromDatePicker()
                if (name.isEmpty() || cigarettes == null || cigarettes == 0) {
                    Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show()
                } else {
                    saveUserToFirestore(name, cigarettes, startDate)
                }
            }
        }
        else {
            binding.singUp.setOnClickListener {
                val name = binding.singUpName.text.toString()
                val email = binding.singUpEmail.text.toString()
                val password = binding.singUpPassword.text.toString()
                val cigarettes = binding.singUpCigarettes.text.toString().toIntOrNull()
                val startDate = getDateFromDatePicker()

                if (name.isEmpty() || email.isEmpty() || password.isEmpty() || cigarettes == null || cigarettes == 0) {
                    Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show()
                } else if (password.length < 6) {
                    Toast.makeText(
                        this,
                        "Password must be at least 6 characters",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    registerUser(name, email, password, cigarettes, startDate)
                }
            }
        }
    }

    private fun getDateFromDatePicker(): Long {
        val day = binding.singUpStartDate.dayOfMonth
        val month = binding.singUpStartDate.month
        val year = binding.singUpStartDate.year

        val calendar = Calendar.getInstance()
        calendar.set(year, month, day, 0, 0, 0)
        return calendar.timeInMillis
    }
    private fun saveUserToFirestore(name: String, cigarettes: Int, startDate: Long) {
        val userId = auth.currentUser?.uid ?: return
        val email = auth.currentUser?.email ?: ""

        val user = mapOf(
            "name" to name,
            "email" to email,
            "cigarettes" to cigarettes,
            "startDate" to startDate,
            "userId" to userId
        )

        firestore.collection("users").document(userId)
            .set(user)
            .addOnSuccessListener {
                Toast.makeText(this, "User registered successfully!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error saving user: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("FirestoreError", "Error saving user", e) // הוספת לוג למעקב
            }
    }

    private fun registerUser(name: String, email: String, password: String, cigarettes: Int, startDate: Long) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    val user = mapOf(
                        "name" to name,
                        "email" to email,
                        "cigarettes" to cigarettes,
                        "startDate" to startDate,
                        "userId" to userId
                    )
                    firestore.collection("users").document(userId!!)
                        .set(user)
                        .addOnSuccessListener {
                            Toast.makeText(this, "User registered successfully!", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Error saving user: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                } else {
                    Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
}
