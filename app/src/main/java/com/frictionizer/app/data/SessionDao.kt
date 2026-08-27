package com.frictionizer.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

data class AppTotal(val packageName: String, val appLabel: String, val totalMs: Long)
data class ActivityTotal(val activityName: String, val totalMs: Long)
data class AppActivityTotal(
    val packageName: String,
    val appLabel: String,
    val activityName: String,
    val totalMs: Long
)

@Dao
interface SessionDao {

    @Insert
    suspend fun insert(session: Session)

    @Query("SELECT * FROM sessions ORDER BY startTime DESC")
    suspend fun getAllSessions(): List<Session>

    @Query("SELECT packageName, appLabel, SUM(durationMs) as totalMs FROM sessions GROUP BY packageName ORDER BY totalMs DESC")
    suspend fun getTotalByApp(): List<AppTotal>

    @Query("SELECT activityName, SUM(durationMs) as totalMs FROM sessions GROUP BY activityName ORDER BY totalMs DESC")
    suspend fun getTotalByActivity(): List<ActivityTotal>

    @Query("SELECT packageName, appLabel, activityName, SUM(durationMs) as totalMs FROM sessions GROUP BY packageName, activityName ORDER BY packageName, totalMs DESC")
    suspend fun getDetailedBreakdown(): List<AppActivityTotal>

    @Query("DELETE FROM sessions")
    suspend fun deleteAll()
}
