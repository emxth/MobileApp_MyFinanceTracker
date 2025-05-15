package data.repository

import androidx.lifecycle.LiveData
import com.example.myfinancetracker.MonthlySummary
import data.dao.TransactionDao
import data.entity.TransactionEntity

class TransactionRepository(private val transactionDao: TransactionDao) {

    fun getAllTransactions(): LiveData<List<TransactionEntity>> = transactionDao.getAllTransactions()

    suspend fun insert(transaction: TransactionEntity) = transactionDao.insertTransaction(transaction)

    suspend fun update(transaction: TransactionEntity) = transactionDao.updateTransaction(transaction)

    suspend fun delete(transaction: TransactionEntity) = transactionDao.deleteTransaction(transaction)

    fun getMonthlySummary(userId: Int, month: Int, year: Int): LiveData<MonthlySummary> {
        val monthStr = String.format("%02d", month)
        val yearStr = year.toString()
        return transactionDao.getMonthlySummary(userId, monthStr, yearStr)
    }
}
