package com.example.questmvp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quests")
data class Quest(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val type: QuestType,
    val isCompleted: Boolean = false,
    val expReward: Int = 10,
    val periodKey: String,
    val lastRewardedPeriodKey: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
