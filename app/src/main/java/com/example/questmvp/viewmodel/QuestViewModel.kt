package com.example.questmvp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.questmvp.data.Quest
import com.example.questmvp.data.QuestRepository
import com.example.questmvp.data.QuestType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class QuestViewModel(private val repository: QuestRepository) : ViewModel() {
    val uiState = combine(
        repository.quests,
        repository.totalExp,
        repository.nickname,
        repository.equippedTitle,
        repository.dailyHistory
    ) { quests, totalExp, nickname, equippedTitle, dailyHistory ->
        quests.toUiState(totalExp, nickname, equippedTitle, dailyHistory)
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = QuestUiState()
        )

    init {
        viewModelScope.launch {
            repository.resetExpiredQuests()
        }
    }

    fun addQuest(title: String, type: QuestType, expReward: Int) {
        viewModelScope.launch {
            repository.addQuest(title, type, expReward)
        }
    }

    fun setCompleted(quest: Quest, completed: Boolean) {
        viewModelScope.launch {
            repository.setCompleted(quest, completed)
        }
    }

    fun deleteQuest(quest: Quest) {
        viewModelScope.launch {
            repository.deleteQuest(quest)
        }
    }

    fun resetLevel() {
        repository.resetLevel()
    }

    fun setNickname(nickname: String) {
        repository.setNickname(nickname)
    }

    fun equipTitle(title: String) {
        repository.equipTitle(title)
    }

    private fun List<Quest>.toUiState(
        totalExp: Int,
        nickname: String,
        equippedTitle: String,
        dailyHistory: Map<String, Int>
    ): QuestUiState {
        val daily = filter { it.type == QuestType.DAILY }
        val weekly = filter { it.type == QuestType.WEEKLY }
        val levelInfo = calculateLevelInfo(totalExp)
        val dailyPercent = daily.percent()

        repository.recordTodayDailyPercent(dailyPercent)

        return QuestUiState(
            quests = this,
            dailyProgress = daily.progress(),
            weeklyProgress = weekly.progress(),
            dailyPercent = dailyPercent,
            weeklyPercent = weekly.percent(),
            earnedExp = totalExp,
            level = levelInfo.level,
            expInCurrentLevel = levelInfo.expInCurrentLevel,
            expForNextLevel = levelInfo.expForNextLevel,
            expToNextLevel = levelInfo.expToNextLevel,
            levelProgress = levelInfo.progress,
            nickname = nickname,
            equippedTitle = equippedTitle,
            isVeteranTitleUnlocked = hasSevenDayDailyPercent(dailyHistory, 100),
            isLazyTitleUnlocked = hasSevenDayDailyPercent(dailyHistory, 0)
        )
    }

    private fun List<Quest>.progress(): Float {
        if (isEmpty()) return 0f
        return count { it.isCompleted }.toFloat() / size.toFloat()
    }

    private fun List<Quest>.percent(): Int = (progress() * 100).toInt()

    private fun calculateLevelInfo(totalExp: Int): LevelInfo {
        var level = 1
        var remainingExp = totalExp.coerceAtLeast(0)
        var requiredExp = expRequiredForNextLevel(level)

        while (remainingExp >= requiredExp) {
            remainingExp -= requiredExp
            level += 1
            requiredExp = expRequiredForNextLevel(level)
        }

        return LevelInfo(
            level = level,
            expInCurrentLevel = remainingExp,
            expForNextLevel = requiredExp,
            expToNextLevel = requiredExp - remainingExp,
            progress = remainingExp.toFloat() / requiredExp.toFloat()
        )
    }

    private fun expRequiredForNextLevel(level: Int): Int {
        return when {
            level < 5 -> 100 + ((level - 1) * 25)
            level < 10 -> 175 + ((level - 4) * 50)
            else -> 425 + ((level - 9) * 100)
        }
    }

    private fun hasSevenDayDailyPercent(history: Map<String, Int>, targetPercent: Int): Boolean {
        val today = LocalDate.now()
        return (0L..6L).all { daysAgo ->
            history[today.minusDays(daysAgo).toString()] == targetPercent
        }
    }

    private data class LevelInfo(
        val level: Int,
        val expInCurrentLevel: Int,
        val expForNextLevel: Int,
        val expToNextLevel: Int,
        val progress: Float
    )
}

class QuestViewModelFactory(
    private val repository: QuestRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QuestViewModel::class.java)) {
            return QuestViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
