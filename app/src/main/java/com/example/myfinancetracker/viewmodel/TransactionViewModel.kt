package com.example.myfinancetracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.myfinancetracker.MonthlySummary
import com.example.myfinancetracker.Transaction
import data.AppDatabase
import data.entity.TransactionEntity
import data.repository.TransactionRepository
import kotlinx.coroutines.launch
import java.util.Calendar

class TransactionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TransactionRepository

    init {
        val transactionDao = AppDatabase.getDatabase(application).transactionDao()
        repository = TransactionRepository(transactionDao)
    }

    fun getAllTransactions(): LiveData<List<TransactionEntity>> = repository.getAllTransactions()

    fun insert(transaction: TransactionEntity) = viewModelScope.launch {
        repository.insert(transaction)
    }

    fun update(transaction: TransactionEntity) = viewModelScope.launch {
        repository.update(transaction)
    }

    fun delete(transaction: TransactionEntity) = viewModelScope.launch {
        repository.delete(transaction)
    }

    fun getMonthlySummary(userId: Int, month: Int, year: Int): LiveData<MonthlySummary> {
        return repository.getMonthlySummary(userId, month, year)
    }


}

