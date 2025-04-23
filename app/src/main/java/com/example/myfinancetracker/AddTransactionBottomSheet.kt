package com.example.myfinancetracker

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Calendar

class AddTransactionBottomSheet : BottomSheetDialogFragment() {
    private var transactionSavedListener: OnTransactionSavedListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_add_transaction, container, false)
    }

    @SuppressLint("CutPasteId", "SetTextI18n", "DefaultLocale")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val spinner = view.findViewById<Spinner>(R.id.spinner_category)
        val editDate = view.findViewById<EditText>(R.id.edit_date)
        val amountEdit = view.findViewById<EditText>(R.id.edit_amount)
        val titleEdit = view.findViewById<EditText>(R.id.edit_title)
        val addButton = view.findViewById<Button>(R.id.btn_add_transaction)

        val sharedPref = requireContext().getSharedPreferences("transactions", Context.MODE_PRIVATE)
        val gson = Gson()

        // Set up the Switch
        // Safely initialize views with null checks
        val addIncomeBtn = view.findViewById<Button>(R.id.btn_add_income)?.apply {
            setOnClickListener {
                view.findViewById<SwitchMaterial>(R.id.type_switch)?.isChecked = true
            }
        }
        val addExpenseBtn = view.findViewById<Button>(R.id.btn_add_expense)?.apply {
            setOnClickListener {
                view.findViewById<SwitchMaterial>(R.id.type_switch)?.isChecked = false
            }
        }
        // Initialize switch and set default state
        val typeSwitch = view.findViewById<SwitchMaterial>(R.id.type_switch)?.apply {
            isChecked = true // Default to income
        }

        // Setup selected item values for updating
        // Set up the Spinner adapter first so it's available
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.transaction_categories,
            R.layout.custom_spinner_item
        ).apply {
            setDropDownViewResource(R.layout.custom_spinner_item)
        }
        spinner.adapter = adapter

        // Setup selected item values for updating
        val transactionToEdit = arguments?.getParcelable<Transaction>("transaction")
        val existingKey = arguments?.getString("key")

        if (transactionToEdit != null) {
            // Set the values in UI
            spinner.setSelection(adapter.getPosition(transactionToEdit.category))
            titleEdit.setText(transactionToEdit.title)
            amountEdit.setText(transactionToEdit.amount.toString())
            editDate.setText(transactionToEdit.date)
            typeSwitch?.isChecked = transactionToEdit.type == "expense"

            // Update button text
            addButton.text = "Update Transaction"
        }

        // Set up the Date picker
        editDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, selectedYear, selectedMonth, selectedDay ->
                    val selectedDate = String.format("%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear)
                    editDate.setText(selectedDate)
                },
                year,
                month,
                day
            )
            datePickerDialog.show()
        }

        // Set up the Spinner for Category dropdown
        // Create an ArrayAdapter using the string array and a default spinner layout
        val adapterDefault = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.transaction_categories,
            R.layout.custom_spinner_item
        ).apply {
            setDropDownViewResource(R.layout.custom_spinner_item)
        }

        // Apply the adapter to the spinner
        spinner.adapter = adapterDefault

        // Adding new transaction
        addButton.setOnClickListener {
            val category = spinner.selectedItem.toString()
            val title = titleEdit.text.toString()
            val amount = amountEdit.text.toString().toDoubleOrNull()
            val date = editDate.text.toString()
            val type = if (typeSwitch?.isChecked == true) "expense" else "income"

            if (title.isEmpty() || amount == null || date.isEmpty()) {
                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val transaction = Transaction(category, title, amount, date, type)
            val editor = sharedPref.edit()

            if (existingKey != null) {
                // Edit existing transaction
                editor.putString(existingKey, gson.toJson(transaction)).apply()
                Toast.makeText(context, "Transaction updated", Toast.LENGTH_SHORT).show()
                transactionSavedListener?.onTransactionSaved()
            } else {
                // Add new
                val key = "transaction_${System.currentTimeMillis()}"
                editor.putString(key, gson.toJson(transaction)).apply()
                Toast.makeText(context, "Transaction added", Toast.LENGTH_SHORT).show()
            }

            dismiss()

            // Optionally: clear inputs
            titleEdit.text.clear()
            amountEdit.text.clear()
            editDate.text.clear()
            spinner.setSelection(0)
            typeSwitch?.isChecked = true
        }
    }

    interface OnTransactionSavedListener {
        fun onTransactionSaved()
    }

    fun setOnTransactionSavedListener(listener: OnTransactionSavedListener) {
        this.transactionSavedListener = listener
    }

    companion object {
        fun newInstance(transaction: Transaction, key: String): AddTransactionBottomSheet {
            val fragment = AddTransactionBottomSheet()
            val args = Bundle()
            args.putParcelable("transaction", transaction)
            args.putString("key", key)
            fragment.arguments = args
            return fragment
        }
    }
}
