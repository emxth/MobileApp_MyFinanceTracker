package com.example.myfinancetracker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContentProviderCompat.requireContext
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class FinanceSummary(
    val income: Double,
    val expense: Double,
    val balance: Double
)

fun getMonthlyFinanceSummaryAndCheckBudget(context: Context): FinanceSummary {
    val prefs = context.getSharedPreferences("transactions", Context.MODE_PRIVATE)
    val budgetPrefs = context.getSharedPreferences("budget", Context.MODE_PRIVATE)
    val notifyPrefs = context.getSharedPreferences("notification_settings", Context.MODE_PRIVATE)

    // Date formatting
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

    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val monthFormat = SimpleDateFormat("MM-yyyy", Locale.getDefault())

    val currentMonth = monthFormat.format(Date()) // e.g., 04-2025

    val currentMonthTransactions = allTransactions.mapNotNull {
        try {
            val date = dateFormat.parse(it.date)
            if (date != null && monthFormat.format(date) == currentMonth) it else null
        } catch (e: Exception) {
            null
        }
    }

    val income = currentMonthTransactions
        .filter { it.type.equals("income", ignoreCase = true) }
        .sumOf { it.amount }

    val expense = currentMonthTransactions
        .filter { it.type.equals("expense", ignoreCase = true) }
        .sumOf { it.amount }

    val balance = income - expense

    // Budget notification logic with proper type conversion
    val monthlyBudget = budgetPrefs.getFloat("budget_$currentMonth", 0f).toDouble()
    val notifyExceed = notifyPrefs.getBoolean("notify_exceed", true)
    val notifyReaching = notifyPrefs.getBoolean("notify_reaching", true)

    if (monthlyBudget > 0) {
        when {
            notifyExceed && expense > monthlyBudget ->
                showBudgetNotification(
                    context,
                    "Budget Exceeded!",
                    "You're ${expense - monthlyBudget} over budget."
                )
            notifyReaching && expense >= 0.8 * monthlyBudget && expense <= monthlyBudget ->
                showBudgetNotification(
                    context,
                    "Budget Alert!",
                    "You've used ${(expense/monthlyBudget*100).toInt()}% of budget."
                )
        }
    }

    return FinanceSummary(income, expense, balance)
}

private fun showBudgetNotification(context: Context, title: String, message: String) {
    val channelId = "budget_channel"
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // Create channel only if needed
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        if (notificationManager.getNotificationChannel(channelId) == null) {
            val channel = NotificationChannel(
                channelId,
                "Budget Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Budget limit notifications"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    NotificationCompat.Builder(context, channelId)
        .setSmallIcon(android.R.drawable.ic_dialog_alert)
        .setContentTitle(title)
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .build()
        .let { notificationManager.notify(System.currentTimeMillis().toInt(), it) }
}

