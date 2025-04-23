package com.example.myfinancetracker

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TransactionsFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TransactionAdapter
    private val transactions = mutableListOf<Transaction>()
    private val filteredTransactions = mutableListOf<Transaction>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_transactions, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set header title
        val headerTitle = view.findViewById<TextView>(R.id.pageTitle)
        headerTitle?.text = "Transactions"

        // Setup RecyclerView
        recyclerView = view.findViewById(R.id.transaction_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Search EditText
        val searchEditText = view.findViewById<EditText>(R.id.search_edit_text)
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val keyword = s.toString().lowercase()
                filterTransactions(keyword)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        loadTransactions()
    }

    // Load all transactions
    private fun loadTransactions() {
        transactions.clear() // Clear old items before loading

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
            .sortedByDescending { transaction ->
                // Parse the date to sort (assumes date format is "dd/MM/yyyy")
                try {
                    val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    format.parse(transaction.date)
                } catch (e: Exception) {
                    Date(0) // fallback to oldest date if parsing fails
                }
            }

        transactions.addAll(loadedTransactions)
        filteredTransactions.clear()
        filteredTransactions.addAll(transactions)

        // Only initialize adapter once
        adapter = TransactionAdapter(filteredTransactions) { transaction, position, anchorView ->
            showContextMenu(transaction, position, anchorView)
        }

        recyclerView.adapter = adapter
    }

    // Setup filter transactions
    @SuppressLint("NotifyDataSetChanged")
    private fun filterTransactions(keyword: String) {
        filteredTransactions.clear()

        if (keyword.isEmpty()) {
            filteredTransactions.addAll(transactions)
        } else {
            filteredTransactions.addAll(transactions.filter {
                it.title.lowercase().contains(keyword) ||
                        it.category.lowercase().contains(keyword) ||
                        it.date.lowercase().contains(keyword)
            })
        }

        adapter.notifyDataSetChanged()
    }

    // Preview long touch popup box
    private fun showContextMenu(transaction: Transaction, position: Int, anchorView: View) {
        val popup = PopupMenu(requireContext(), anchorView)
        popup.menuInflater.inflate(R.menu.transaction_options, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_edit -> {
                    editTransaction(transaction)
                    true
                }
                R.id.menu_delete -> {
                    deleteTransaction(position)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    // Setup edit transactions
    private fun editTransaction(transaction: Transaction) {
        val sharedPref = requireContext().getSharedPreferences("transactions", Context.MODE_PRIVATE)
        val allEntries = sharedPref.all

        val key = allEntries.entries.find { (_, value) ->
            val savedTransaction = Gson().fromJson(value as String, Transaction::class.java)
            savedTransaction == transaction
        }?.key

        if (key != null) {
            val sheet = AddTransactionBottomSheet.newInstance(transaction, key)

            sheet.setOnTransactionSavedListener(object : AddTransactionBottomSheet.OnTransactionSavedListener {
                override fun onTransactionSaved() {
                    loadTransactions()
                }
            })

            sheet.show(parentFragmentManager, "EditTransaction")
        } else {
            Toast.makeText(requireContext(), "Unable to edit transaction", Toast.LENGTH_SHORT).show()
        }
    }



    // Setup delete transactions
    private fun deleteTransaction(position: Int) {
        val transaction = transactions[position]

        AlertDialog.Builder(requireContext())
            .setTitle("Delete Transaction")
            .setMessage("Are you sure?")
            .setPositiveButton("Delete") { _, _ ->
                // Delete from SharedPreferences
                val sharedPreferences = requireContext().getSharedPreferences("transactions", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()

                // Loop through entries to find the exact transaction to delete
                val gson = Gson()
                val allEntries = sharedPreferences.all

                for ((key, value) in allEntries) {
                    if (key.startsWith("transaction_")) {
                        val storedTransaction = try {
                            gson.fromJson(value as String, Transaction::class.java)
                        } catch (e: Exception) {
                            null
                        }

                        if (storedTransaction != null &&
                            storedTransaction.title == transaction.title &&
                            storedTransaction.date == transaction.date &&
                            storedTransaction.amount == transaction.amount &&
                            storedTransaction.category == transaction.category &&
                            storedTransaction.type == transaction.type
                        ) {
                            editor.remove(key) // remove matched transaction
                            break
                        }
                    }
                }

                editor.apply()

                // Remove from list and notify adapter
                transactions.removeAt(position)
                adapter.notifyItemRemoved(position)

                Toast.makeText(requireContext(), "Transaction deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

}