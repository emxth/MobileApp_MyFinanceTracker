package com.example.myfinancetracker

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import data.AppDatabase
import data.dao.UserDao
import data.entity.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SignUpActivity : AppCompatActivity() {
    private lateinit var database: AppDatabase
    private lateinit var userDao: UserDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnSignIn =findViewById<View>(R.id.text_btn_signIn)

        // Initialize Room database
        database = AppDatabase.getDatabase(this)
        userDao = database.userDao()

        val usernameEditText = findViewById<EditText>(R.id.edit_username)
        val passwordEditText = findViewById<EditText>(R.id.edit_password)
        val emailEditText = findViewById<EditText>(R.id.edit_email)
        val mobileEditText = findViewById<EditText>(R.id.edit_mobile)
        val signUpButton = findViewById<MaterialButton>(R.id.btn_signUp)

        signUpButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val mobile = mobileEditText.text.toString().trim()

            // Validate input
            if (username.isEmpty() || password.isEmpty() || email.isEmpty() || mobile.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save user
            saveUser(username, password, email, mobile)
        }

        findViewById<View>(R.id.text_btn_signIn).setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }

        btnSignIn.setOnClickListener{
            // Navigate to Sign in page
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }
    }

    private fun saveUser(username: String, password: String, email: String, mobile: String) {
        // Room operations should be done in a coroutine
        CoroutineScope(Dispatchers.IO).launch {
            val existingUser = userDao.getUserByEmail(email)

            if (existingUser != null) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(applicationContext, "Email already exists!", Toast.LENGTH_SHORT).show()
                }
            } else {
                val newUser = User(
                    username = username,
                    password = password,
                    email = email,
                    mobile = mobile
                )
                userDao.insertUser(newUser)
                withContext(Dispatchers.Main) {
                    Toast.makeText(applicationContext, "Sign Up Successful!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@SignUpActivity, SignInActivity::class.java))
                    finish()
                }
            }
        }
    }
}