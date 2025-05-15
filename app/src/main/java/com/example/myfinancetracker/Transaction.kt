package com.example.myfinancetracker

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.io.Serializable

@Parcelize
@Entity(tableName = "transactions")  // Room annotation
data class Transaction(
    @PrimaryKey(autoGenerate = true)  // Let Room auto-generate IDs
    val id: Int = 0,  // Default value for auto-generation

    val category: String,
    val title: String,
    val amount: Double,
    val date: String,
    val type: String,  // "income" or "expense"
    val userId: Int
) : Parcelable