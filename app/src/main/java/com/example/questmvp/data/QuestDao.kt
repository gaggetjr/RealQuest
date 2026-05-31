package com.example.questmvp.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestDao {
    @Query("SELECT * FROM quests ORDER BY type ASC, createdAt DESC")
    fun observeQuests(): Flow<List<Quest>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuest(quest: Quest)

    @Update
    suspend fun updateQuest(quest: Quest)

    @Delete
    suspend fun deleteQuest(quest: Quest)

    @Query(
        """
        UPDATE quests
        SET isCompleted = 0, periodKey = :currentPeriodKey
        WHERE type = :type AND periodKey != :currentPeriodKey
        """
    )
    suspend fun resetExpiredQuests(type: QuestType, currentPeriodKey: String)
}
