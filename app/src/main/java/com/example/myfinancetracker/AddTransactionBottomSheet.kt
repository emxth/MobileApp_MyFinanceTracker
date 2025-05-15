package com.example.myfinancetracker

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import data.AppDatabase
import data.dao.TransactionDao
import data.entity.TransactionEntity
import kotlinx.coroutines.launch
import util.SessionManager
import java.util.Calendar

class AddTransactionBottomSheet : BottomSheetDialogFragment() {

    private var transactionSavedListener: OnTransactionSavedListener? = null
    private lateinit var db: AppDatabase
    private lateinit var transactionDao: TransactionDao
    private var existingTransaction: Transaction? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_add_transaction, container, false)
    }

    @SuppressLint("SetTextI18n", "DefaultLocale")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val spinner = view.findViewById<Spinner>(R.id.spinner_category)
        val editDate = view.findViewById<EditText>(R.id.edit_date)
        val amountEdit = view.findViewById<EditText>(R.id.edit_amount)
        val titleEdit = view.findViewById<EditText>(R.id.edit_title)
        val addButton = view.findViewById<Button>(R.id.btn_add_transaction)
        val typeSwitch = view.findViewById<SwitchMaterial>(R.id.type_switch)

        db = AppDatabase.getDatabase(requireContext())
        transactionDao = db.transactionDao()

        // Spinner adapter setup
        val spinnerAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.transaction_categories,
            R.layout.custom_spinner_item
        ).apply {
            setDropDownViewResource(R.layout.custom_spinner_item)
        }
        spinner.adapter = spinnerAdapter

        // Set up income/expense toggle buttons
        view.findViewById<Button>(R.id.btn_add_income)?.setOnClickListener {
            typeSwitch.isChecked = true
        }
        view.findViewById<Button>(R.id.btn_add_expense)?.setOnClickListener {
            typeSwitch.isChecked = false
        }

        // Set default switch state
        typeSwitch.isChecked = true

        // Get transaction if editing
        existingTransaction = arguments?.getParcelable(ARG_TRANSACTION)
        existingTransaction?.let {
            spinner.setSelection(spinnerAdapter.getPosition(it.category))
            titleEdit.setText(it.title)
            amountEdit.setText(it.amount.toString())
            editDate.setText(it.date)
            typeSwitch.isChecked = it.type == "expense"
            addButton.text = "Update Transaction"
        }

        // Date picker
        editDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    editDate.setText(String.format("%02d/%02d/%d", day, month + 1, year))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Save transaction (Insert or Update)
        addButton.setOnClickListener {
            val category = spinner.selectedItem.toString()
            val title = titleEdit.text.toString()
            val amount = amountEdit.text.toString().toDoubleOrNull()
            val date = editDate.text.toString()
            val type = if (typeSwitch.isChecked) "expense" else "income"

            if (title.isEmpty() || amount == null || date.isEmpty() || category.isEmpty()) {
                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userId = SessionManager.getUserId(requireContext())
            if (userId == -1) {
                Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val transactionEntity = TransactionEntity(
                id = existingTransaction?.id ?: 0,  // 0 means auto-generate if inserting
                userId = userId,
                category = category,
                title = title,
                amount = amount,
                date = date,
                type = type
            )

            lifecycleScope.launch {
                if (existingTransaction != null) {
                    transactionDao.updateTransaction(transactionEntity)
                    Log.d("Transaction", "Updated: $transactionEntity")
                } else {
                    transactionDao.insertTransaction(transactionEntity)
                    Log.d("Transaction", "Inserted: $transactionEntity")
                }

                Toast.makeText(context, "Transaction saved", Toast.LENGTH_SHORT).show()
                transactionSavedListener?.onTransactionSaved()
                dismiss()
            }
        }
    }

    interface OnTransactionSavedListener {
        fun onTransactionSaved()
    }

    fun setOnTransactionSavedListener(listener: OnTransactionSavedListener) {
        this.transactionSavedListener = listener
    }

    companion object {
        private const val ARG_TRANSACTION = "transaction"

        fun newInstance(transaction: Transaction): AddTransactionBottomSheet {
            val fragment = AddTransactionBottomSheet()
            val args = Bundle()
            args.putParcelable(ARG_TRANSACTION, transaction)
            fragment.arguments = args
            return fragment
        }
    }
}

