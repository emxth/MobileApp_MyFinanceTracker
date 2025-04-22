package com.example.myfinancetracker

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        replaceFragment(DashboardFragment())

        findViewById<BottomNavigationView>(R.id.bottomNavBar).setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.dashboard -> {
                    replaceFragment(DashboardFragment())
                }

                R.id.transactions -> {
                    replaceFragment(TransactionsFragment())
                }

                R.id.budget -> {
                    replaceFragment(BudgetFragment())
                }

                R.id.analytics -> {
                    replaceFragment(AnalyticsFragment())
                }

                R.id.settings -> {
                    replaceFragment(SettingsFragment())
                }
            }

            return@setOnItemSelectedListener true
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentTransaction =supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()
    }
}