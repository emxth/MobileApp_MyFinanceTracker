package data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import data.dao.BudgetDao
import data.dao.NotificationSettingsDao
import data.dao.TransactionDao
import data.dao.UserDao
import data.entity.BudgetEntity
import data.entity.NotificationSettingsEntity
import data.entity.TransactionEntity
import data.entity.User

@Database(entities = [User::class, TransactionEntity::class, BudgetEntity::class, NotificationSettingsEntity::class], version = 3)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun notificationSettingsDao(): NotificationSettingsDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "finance_tracker_db"
                )
                    .fallbackToDestructiveMigration() // Add this during development
                    .build().also { instance = it }
            }
    }
}
