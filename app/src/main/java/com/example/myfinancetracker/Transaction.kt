package com.example.myfinancetracker

import kotlinx.parcelize.Parcelize
import android.os.Parcelable

@Parcelize
data class Transaction (
    val category: String,
    val title: String,
    val amount: Double,
    val date: String,
    val type: String // "income" or "expense"
) : Parcelable