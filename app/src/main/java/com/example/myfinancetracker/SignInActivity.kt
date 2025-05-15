package com.example.myfinancetracker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.myfinancetracker.databinding.ActivitySignInBinding
import com.example.myfinancetracker.viewmodel.UserViewModel

class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding
    private lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Insets handling
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]

        binding.btnSignIn.setOnClickListener {
            val username = binding.editUsername.text.toString().trim()
            val password = binding.editPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else {
                userViewModel.loginUser(username, password) { user ->
                    if (user?.id != null) {
                        val sharedPref = getSharedPreferences("user_session", Context.MODE_PRIVATE)
                        sharedPref.edit().putInt("user_id", user.id).apply()
                        sharedPref.edit().putBoolean("is_logged_in", true).apply()

                        val intent = Intent(this, MainActivity::class.java)
                        // Optional: pass ID if MainActivity needs it directly
                        intent.putExtra("user_id", user.id)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Invalid credentials or user data", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        binding.textBtnSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }
}
