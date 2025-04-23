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

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [DashboardFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DashboardFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set header title
        val headerTitle = view.findViewById<TextView>(R.id.pageTitle)
        headerTitle?.text = "Dashboard"

        val summary = getMonthlyFinanceSummaryAndCheckBudget(requireContext())
        val incomeText = view.findViewById<TextView>(R.id.text_value_income)
        val expenseText = view.findViewById<TextView>(R.id.text_value_expense)
        val balanceText = view.findViewById<TextView>(R.id.balance_text)

        // Set Amount Balance
        incomeText.text = "%.2f".format(summary.income)
        expenseText.text = "%.2f".format(summary.expense)
        balanceText.text = "%.2f".format(summary.balance)

        // Open Add (income) transaction form
        val addIncomeBtn = view.findViewById<Button>(R.id.btn_add_income)
        addIncomeBtn.setOnClickListener {
            val bottomSheet = AddTransactionBottomSheet()
            bottomSheet.show(parentFragmentManager, bottomSheet.tag)
        }

        // Open Add (expense) transaction form
        val addExpenseBtn = view.findViewById<Button>(R.id.btn_add_expense)
        addExpenseBtn.setOnClickListener {
            val bottomSheet = AddTransactionBottomSheet()
            bottomSheet.show(parentFragmentManager, bottomSheet.tag)
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment DashboardFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            DashboardFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

}