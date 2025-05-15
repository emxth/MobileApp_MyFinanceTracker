package data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import data.entity.NotificationSettingsEntity

@Dao
interface NotificationSettingsDao {

    @Query("SELECT * FROM notification_settings WHERE userId = :userId")
    suspend fun getSettings(userId: Int): NotificationSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(settings: NotificationSettingsEntity)
}