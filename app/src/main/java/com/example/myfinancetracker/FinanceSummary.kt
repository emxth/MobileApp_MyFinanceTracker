package com.example.myfinancetracker

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class FinanceSummary(
    val income: Double,
    val expense: Double,
    val balance: Double
)

fun getFinanceSummary(context: Context): FinanceSummary {
    val sharedPreferences = context.getSharedPreferences("transactions", Context.MODE_PRIVATE)
    val gson = Gson()

    // Get ALL stored key-value pairs
    val allEntries = sharedPreferences.all

    // Filter and parse only transaction entries
    val transactions = allEntries
        .filter { it.key.startsWith("transaction_") } // Only get transaction keys
        .mapNotNull { entry ->
            try {
                gson.fromJson(entry.value as String, Transaction::class.java)
            } catch (e: Exception) {
                null // Skip corrupted entries
            }
        }

    val income = transactions.filter { it.type == "income" }.sumOf { it.amount }
    val expense = transactions.filter { it.type == "expense" }.sumOf { it.amount }
    val balance = income - expense

    return FinanceSummary(income, expense, balance)
}

fun getAllTransactions(context: Context): List<Transaction> {
    val sharedPreferences = context.getSharedPreferences("transactions", Context.MODE_PRIVATE)
    val gson = Gson()

    return sharedPreferences.all
        .filter { it.key.startsWith("transaction_") }
        .mapNotNull {
            try {
                gson.fromJson(it.value as String, Transaction::class.java)
            } catch (e: Exception) {
                null
            }
        }
}

