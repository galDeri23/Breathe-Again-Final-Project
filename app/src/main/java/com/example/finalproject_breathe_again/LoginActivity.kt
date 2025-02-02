package com.example.finalproject_breathe_again

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.finalproject_breathe_again.databinding.ActivityLoginBinding
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    // Firebase Sign-In Launcher
    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { res ->
        this.onSignInResult(res)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginLoginButton.setOnClickListener {
            val email = binding.loginEmail.text.toString()
            val password = binding.loginPassword.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show()
            } else if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            } else {
                signInWithEmailAndPassword(email, password)
            }
        }
        binding.loginGoogleButton.setOnClickListener {
            signInWithGoogle()
        }
        binding.loginPhoneButton.setOnClickListener {
            signInWithPhone()
        }
        binding.loginSignup.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun signInWithPhone() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.PhoneBuilder().build()
        )

        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setLogo(R.drawable.logo)
            .build()

        signInLauncher.launch(signInIntent)
    }

    private fun signInWithGoogle() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setLogo(R.drawable.logo)
            .build()

        signInLauncher.launch(signInIntent)
    }

    private fun signInWithEmailAndPassword(email: String, password: String) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Welcome, $email!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        if (result.resultCode == RESULT_OK) {

            val user = FirebaseAuth.getInstance().currentUser
            Toast.makeText(this, "Welcome, ${user?.displayName}!", Toast.LENGTH_SHORT).show()


            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        } else {

            if (response == null) {
                Toast.makeText(this, "Sign-in canceled", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Error: ${response.error?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}