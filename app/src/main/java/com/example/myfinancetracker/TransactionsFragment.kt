package com.example.myfinancetracker

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [TransactionsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TransactionsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TransactionAdapter
    private val transactions = mutableListOf<Transaction>()

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

        transactions.addAll(loadedTransactions)

        // Only initialize adapter once
        adapter = TransactionAdapter(transactions) { transaction, position ->
            showContextMenu(transaction, position)
            true
        }
        recyclerView.adapter = adapter
    }

    // Preview long touch popup box
    private fun showContextMenu(transaction: Transaction, position: Int) {
        val popup = PopupMenu(requireContext(), recyclerView)
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


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment TransactionsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            TransactionsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}