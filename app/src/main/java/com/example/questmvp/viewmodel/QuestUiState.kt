package com.example.questmvp.viewmodel

import com.example.questmvp.data.Quest

data class QuestUiState(
    val quests: List<Quest> = emptyList(),
    val dailyProgress: Float = 0f,
    val weeklyProgress: Float = 0f,
    val dailyPercent: Int = 0,
    val weeklyPercent: Int = 0,
    val earnedExp: Int = 0,
    val level: Int = 1,
    val expInCurrentLevel: Int = 0,
    val expForNextLevel: Int = 100,
    val expToNextLevel: Int = 100,
    val levelProgress: Float = 0f,
    val nickname: String = "",
    val equippedTitle: String = "",
    val isVeteranTitleUnlocked: Boolean = false,
    val isLazyTitleUnlocked: Boolean = false
)
