package data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import data.entity.BudgetEntity

@Dao
interface BudgetDao {

    @Query("SELECT * FROM budgets WHERE userId = :userId AND month = :month LIMIT 1")
    suspend fun getBudget(userId: Int, month: String): BudgetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(budget: BudgetEntity)
}

