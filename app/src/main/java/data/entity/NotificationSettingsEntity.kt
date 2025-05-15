package data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification_settings")
data class NotificationSettingsEntity(
    @PrimaryKey val userId: Int,
    val notifyExceed: Boolean = true,
    val notifyReaching: Boolean = true
)