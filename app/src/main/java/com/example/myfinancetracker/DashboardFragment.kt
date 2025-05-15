package com.example.myfinancetracker

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import android.content.SharedPreferences
import androidx.lifecycle.ViewModelProvider
import com.example.myfinancetracker.viewmodel.TransactionViewModel
import util.SessionManager
import java.util.Calendar

class DashboardFragment : Fragment() {

    private lateinit var transactionViewModel: TransactionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModel
        transactionViewModel = ViewModelProvider(requireActivity())[TransactionViewModel::class.java]

        // Set header
        val headerTitle = view.findViewById<TextView>(R.id.pageTitle)
        headerTitle.text = "Dashboard"

        val incomeText = view.findViewById<TextView>(R.id.text_value_income)
        val expenseText = view.findViewById<TextView>(R.id.text_value_expense)
        val balanceText = view.findViewById<TextView>(R.id.balance_text)

        // Get current month and year
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH) + 1 // Months are 0-indexed
        val currentYear = calendar.get(Calendar.YEAR)

        val userId = SessionManager.getUserId(requireContext())

        // Observe summary for current month
        transactionViewModel.getMonthlySummary(userId, currentMonth, currentYear)
            .observe(viewLifecycleOwner) { summary ->
                val (income, expense, balance) = summary

                incomeText.text = "%.2f".format(income)
                expenseText.text = "%.2f".format(expense)
                balanceText.text = "%.2f".format(balance)
            }

        // Open Add Income bottom sheet
        view.findViewById<Button>(R.id.btn_add_income).setOnClickListener {
            val bottomSheet = AddTransactionBottomSheet()
            bottomSheet.show(parentFragmentManager, bottomSheet.tag)
        }

        // Open Add Expense bottom sheet
        view.findViewById<Button>(R.id.btn_add_expense).setOnClickListener {
            val bottomSheet = AddTransactionBottomSheet()
            bottomSheet.show(parentFragmentManager, bottomSheet.tag)
        }
    }

    companion object {}
}
