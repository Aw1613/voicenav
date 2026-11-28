package com.elderassist.app.data.history

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

enum class ActivityOutcome {
    SUCCESS,
    CANCELLED,
    BLOCKED,
    INFO
}

@Entity(tableName = "activity_events")
data class ActivityEventEntity(
    @PrimaryKey
    val id: String,
    val occurredAtEpochMs: Long,
    val actionCode: String,
    val outcome: String,
    val userSummary: String,
    val detail: String?
)

@Dao
interface ActivityEventDao {
    @Query("SELECT * FROM activity_events ORDER BY occurredAtEpochMs DESC LIMIT 100")
    fun observeRecent(): Flow<List<ActivityEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: ActivityEventEntity)

    @Query("DELETE FROM activity_events")
    suspend fun clearAll()
}

@Database(
    entities = [ActivityEventEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ElderAssistDatabase : RoomDatabase() {
    abstract fun activityEventDao(): ActivityEventDao
}
