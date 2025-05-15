package com.example.myfinancetracker

import android.annotation.SuppressLint
import android.app.AlertDialog
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
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myfinancetracker.viewmodel.TransactionViewModel
import data.entity.TransactionEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TransactionsFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TransactionAdapter
    private val transactions = mutableListOf<Transaction>()
    private val filteredTransactions = mutableListOf<Transaction>()
    private lateinit var transactionViewModel: TransactionViewModel

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

        // Initialize RecyclerView FIRST
        recyclerView = view.findViewById(R.id.transaction_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Init ViewModel
        transactionViewModel = ViewModelProvider(this)[TransactionViewModel::class.java]

        adapter = TransactionAdapter(filteredTransactions) { transaction, position, anchorView ->
            showContextMenu(transaction, position, anchorView)
        }
        recyclerView.adapter = adapter

        // Observe and load all transactions from database
        transactionViewModel.getAllTransactions().observe(viewLifecycleOwner) { dbTransactions ->
            transactions.clear()
            transactions.addAll(dbTransactions.map {
                Transaction(
                    id = it.id,  // Ensure your Transaction model includes `id`
                    title = it.title,
                    amount = it.amount,
                    category = it.category,
                    date = it.date,
                    type = it.type,
                    userId = it.userId
                )
            }.sortedByDescending {
                try {
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(it.date)
                } catch (e: Exception) {
                    Date(0)
                }
            })
            filterTransactions("") // Refresh filtered list
        }

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
    }

    // Load all transactions
    @SuppressLint("NotifyDataSetChanged")
    private fun loadTransactions() {
        transactionViewModel.getAllTransactions().observe(viewLifecycleOwner) { loadedEntities ->
            transactions.clear()

            val convertedAndSorted = loadedEntities.map {
                Transaction(
                    id = it.id,
                    title = it.title,
                    amount = it.amount,
                    category = it.category,
                    date = it.date,
                    type = it.type,
                    userId = it.userId
                )
            }.sortedByDescending {
                try {
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(it.date)
                } catch (e: Exception) {
                    Date(0)
                }
            }

            transactions.addAll(convertedAndSorted)

            filteredTransactions.clear()
            filteredTransactions.addAll(transactions)

            if (!::adapter.isInitialized) {
                adapter = TransactionAdapter(filteredTransactions) { transaction, position, anchorView ->
                    showContextMenu(transaction, position, anchorView)
                }
                recyclerView.adapter = adapter
            } else {
                adapter.notifyDataSetChanged()
            }
        }
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
        val sheet = AddTransactionBottomSheet.newInstance(transaction)

        sheet.setOnTransactionSavedListener(object :
            AddTransactionBottomSheet.OnTransactionSavedListener {
            override fun onTransactionSaved() {
                loadTransactions()
            }
        })

        sheet.show(parentFragmentManager, "EditTransaction")
    }

    // Setup delete transactions
    private fun deleteTransaction(position: Int) {
        val transaction = transactions[position]
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Transaction")
            .setMessage("Are you sure?")
            .setPositiveButton("Delete") { _, _ ->
                val entity = TransactionEntity(
                    id = transaction.id,
                    title = transaction.title,
                    amount = transaction.amount,
                    category = transaction.category,
                    date = transaction.date,
                    type = transaction.type,
                    userId = transaction.userId
                )
                transactionViewModel.delete(entity)
                Toast.makeText(requireContext(), "Transaction deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

}