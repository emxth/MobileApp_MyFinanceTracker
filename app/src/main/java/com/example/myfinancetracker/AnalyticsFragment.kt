package com.example.myfinancetracker

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import com.google.gson.Gson
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.animation.Easing
import java.text.SimpleDateFormat
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AnalyticsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AnalyticsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var expensePieChart: PieChart
    private lateinit var incomePieChart: PieChart
    private val transactions = mutableListOf<Transaction>()
    private lateinit var monthTextView: TextView
    private lateinit var monthPickerButton: ImageButton
    private var selectedMonthYear: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_analytics, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set header title
        val headerTitle = view.findViewById<TextView>(R.id.pageTitle)
        headerTitle?.text = "Analytics"

        val totalIncomeText = view.findViewById<TextView>(R.id.text_total_income)
        val totalExpenseText = view.findViewById<TextView>(R.id.text_total_expense)
        val totalBalanceText = view.findViewById<TextView>(R.id.text_total_balance)

        expensePieChart = view.findViewById(R.id.expensePieChart)
        incomePieChart = view.findViewById(R.id.incomePieChart)
        monthTextView = view.findViewById(R.id.monthTextView)
        monthPickerButton = view.findViewById(R.id.monthPickerButton)

        loadTotalFinanceStats(totalIncomeText, totalExpenseText, totalBalanceText)

        updateMonthLabel()
        loadTransactions()

        monthPickerButton.setOnClickListener {
            showMonthPicker()
        }
    }

    // Load total balance, expense and income from entire data set (without filtering)
    private fun loadTotalFinanceStats(incomeView: TextView, expenseView: TextView, balanceView: TextView) {
        val prefs = requireContext().getSharedPreferences("transactions", Context.MODE_PRIVATE)
        val gson = Gson()

        val allTransactions = prefs.all
            .filter { it.key.startsWith("transaction_") }
            .mapNotNull {
                try {
                    gson.fromJson(it.value as? String, Transaction::class.java)
                } catch (e: Exception) {
                    null
                }
            }

        val totalIncome = allTransactions
            .filter { it.type.equals("income", ignoreCase = true) }
            .sumOf { it.amount }

        val totalExpense = allTransactions
            .filter { it.type.equals("expense", ignoreCase = true) }
            .sumOf { it.amount }

        val balance = totalIncome - totalExpense

        incomeView.text = "LKR %.2f".format(totalIncome)
        expenseView.text = "LKR %.2f".format(totalExpense)
        balanceView.text = "LKR %.2f".format(balance)
    }

    // Load all transactions
    private fun loadTransactions() {
        transactions.clear()

        val sharedPreferences = requireContext().getSharedPreferences("transactions", Context.MODE_PRIVATE)
        val gson = Gson()

        val allEntries = sharedPreferences.all
        val loadedTransactions = allEntries
            .filter { it.key.startsWith("transaction_") }
            .mapNotNull { entry ->
                try {
                    gson.fromJson(entry.value as String, Transaction::class.java)
                } catch (e: Exception) {
                    null
                }
            }
            .filter { transaction ->
                // Filter by selected month
                try {
                    val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) // Adjust to your date format
                    val date = format.parse(transaction.date)
                    val cal = Calendar.getInstance().apply { time = date!! }
                    cal.get(Calendar.MONTH) == selectedMonthYear.get(Calendar.MONTH) &&
                            cal.get(Calendar.YEAR) == selectedMonthYear.get(Calendar.YEAR)
                } catch (e: Exception) {
                    false
                }
            }

        transactions.addAll(loadedTransactions)
        showPieCharts()
    }

    // Setup and update month label
    private fun updateMonthLabel() {
        val format = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        monthTextView.text = format.format(selectedMonthYear.time)
    }

    // Show month picker
    private fun showMonthPicker() {
        val year = selectedMonthYear.get(Calendar.YEAR)
        val month = selectedMonthYear.get(Calendar.MONTH)

        val datePicker = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, _ ->
            selectedMonthYear.set(Calendar.YEAR, selectedYear)
            selectedMonthYear.set(Calendar.MONTH, selectedMonth)
            updateMonthLabel()
            loadTransactions()
        }, year, month, 1)

        // Hide day picker
        datePicker.datePicker.findViewById<View>(
            Resources.getSystem().getIdentifier("day", "id", "android")
        )?.visibility = View.GONE

        datePicker.show()
    }

    // Pass parameters for show pie charts (income and expense)
    private fun showPieCharts() {
        val expenseTotals = mutableMapOf<String, Float>()
        val incomeTotals = mutableMapOf<String, Float>()

        for (transaction in transactions) {
            val map = if (transaction.type == "expense") expenseTotals else incomeTotals
            val current = map[transaction.category] ?: 0f
            map[transaction.category] = (current + transaction.amount).toFloat()
        }

        setupPieChart(expensePieChart, expenseTotals, "Expenses")
        setupPieChart(incomePieChart, incomeTotals, "Incomes")
    }

    // Setup pie charts
    private fun setupPieChart(chart: PieChart, data: Map<String, Float>, centerText: String) {
        val entries = data.map { PieEntry(it.value, it.key) }

        val dataSet = PieDataSet(entries, "")
        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
        dataSet.valueTextSize = 14f

        val pieData = PieData(dataSet)

        chart.data = pieData
        chart.description.isEnabled = false
        chart.centerText = centerText
        chart.setEntryLabelTextSize(12f)
        chart.animateY(1000, Easing.EaseInOutQuad)
        chart.invalidate()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AnalyticsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AnalyticsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}