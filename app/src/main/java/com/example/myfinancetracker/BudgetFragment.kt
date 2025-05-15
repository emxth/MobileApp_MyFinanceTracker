package com.example.myfinancetracker

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import data.AppDatabase
import data.dao.BudgetDao
import data.entity.BudgetEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import util.SessionManager
import java.text.SimpleDateFormat
import java.util.*

class BudgetFragment : Fragment() {
    private lateinit var db: AppDatabase
    private lateinit var budgetDao: BudgetDao

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_budget, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val headerTitle = view.findViewById<TextView>(R.id.pageTitle)
        val budgetEditText = view.findViewById<EditText>(R.id.budget_edit_text)

        headerTitle.text = "Budget"

        val userId = SessionManager.getUserId(requireContext())
        val currentMonth = SimpleDateFormat("MM-yyyy", Locale.getDefault()).format(Date())

        db = AppDatabase.getDatabase(requireContext())
        budgetDao = db.budgetDao()

        // Load existing budget
        lifecycleScope.launch {
            try {
                val budget = withContext(Dispatchers.IO) {
                    budgetDao.getBudget(userId, currentMonth)
                }
                budgetEditText.setText(budget?.amount?.toString() ?: "")
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Error loading budget", Toast.LENGTH_SHORT).show()
            }
        }

        // Save budget on blur
        budgetEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val amount = budgetEditText.text.toString().toFloatOrNull() ?: 0f
                val newBudget = BudgetEntity(
                    id = 0,
                    userId = userId,
                    month = currentMonth,
                    amount = amount
                )

                lifecycleScope.launch {
                    try {
                        withContext(Dispatchers.IO) {
                            budgetDao.insertOrUpdate(newBudget)
                        }
                        Toast.makeText(requireContext(), "Budget saved", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(requireContext(), "Error saving budget", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}

