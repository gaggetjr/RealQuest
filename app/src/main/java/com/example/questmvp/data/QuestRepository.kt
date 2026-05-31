package com.example.questmvp.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.Locale

class QuestRepository(
    private val dao: QuestDao,
    context: Context
) {
    private val prefs = context.getSharedPreferences("quest_progress", Context.MODE_PRIVATE)
    private val _totalExp = MutableStateFlow(prefs.getInt(KEY_TOTAL_EXP, 0))
    private val _nickname = MutableStateFlow(prefs.getString(KEY_NICKNAME, "").orEmpty())
    private val _equippedTitle = MutableStateFlow(prefs.getString(KEY_EQUIPPED_TITLE, "").orEmpty())
    private val _dailyHistory = MutableStateFlow(loadDailyHistory())

    val quests: Flow<List<Quest>> = dao.observeQuests()
    val totalExp: StateFlow<Int> = _totalExp
    val nickname: StateFlow<String> = _nickname
    val equippedTitle: StateFlow<String> = _equippedTitle
    val dailyHistory: StateFlow<Map<String, Int>> = _dailyHistory

    suspend fun addQuest(title: String, type: QuestType, expReward: Int) {
        val trimmedTitle = title.trim()
        if (trimmedTitle.isEmpty()) return

        dao.insertQuest(
            Quest(
                title = trimmedTitle,
                type = type,
                expReward = expReward.coerceAtLeast(1),
                periodKey = periodKeyFor(type)
            )
        )
    }

    suspend fun setCompleted(quest: Quest, completed: Boolean) {
        val currentPeriodKey = periodKeyFor(quest.type)
        val shouldReward = completed &&
            !quest.isCompleted &&
            quest.lastRewardedPeriodKey != currentPeriodKey

        if (shouldReward) {
            addExp(quest.expReward)
        }

        dao.updateQuest(
            quest.copy(
                isCompleted = completed,
                periodKey = currentPeriodKey,
                lastRewardedPeriodKey = if (shouldReward) currentPeriodKey else quest.lastRewardedPeriodKey
            )
        )
    }

    suspend fun deleteQuest(quest: Quest) {
        dao.deleteQuest(quest)
    }

    suspend fun resetExpiredQuests() {
        dao.resetExpiredQuests(QuestType.DAILY, periodKeyFor(QuestType.DAILY))
        dao.resetExpiredQuests(QuestType.WEEKLY, periodKeyFor(QuestType.WEEKLY))
    }

    fun resetLevel() {
        prefs.edit()
            .putInt(KEY_TOTAL_EXP, 0)
            .putString(KEY_EQUIPPED_TITLE, "")
            .apply()
        _totalExp.value = 0
        _equippedTitle.value = ""
    }

    fun setNickname(nickname: String) {
        val cleanName = nickname.trim().take(12)
        prefs.edit().putString(KEY_NICKNAME, cleanName).apply()
        _nickname.value = cleanName
    }

    fun equipTitle(title: String) {
        prefs.edit().putString(KEY_EQUIPPED_TITLE, title).apply()
        _equippedTitle.value = title
    }

    fun recordTodayDailyPercent(percent: Int) {
        val today = LocalDate.now().toString()
        if (_dailyHistory.value[today] == percent) return

        val updated = (_dailyHistory.value + (today to percent))
            .entries
            .sortedByDescending { LocalDate.parse(it.key) }
            .take(14)
            .associate { it.key to it.value }

        prefs.edit().putString(KEY_DAILY_HISTORY, serializeDailyHistory(updated)).apply()
        _dailyHistory.value = updated
    }

    private fun addExp(exp: Int) {
        val newTotal = (_totalExp.value + exp).coerceAtLeast(0)
        prefs.edit().putInt(KEY_TOTAL_EXP, newTotal).apply()
        _totalExp.value = newTotal
    }

    fun periodKeyFor(type: QuestType): String {
        val today = LocalDate.now()
        return when (type) {
            QuestType.DAILY -> today.toString()
            QuestType.WEEKLY -> {
                val weekFields = WeekFields.of(Locale.getDefault())
                val week = today.get(weekFields.weekOfWeekBasedYear())
                val year = today.get(weekFields.weekBasedYear())
                "$year-W$week"
            }
        }
    }

    companion object {
        private const val KEY_TOTAL_EXP = "total_exp"
        private const val KEY_NICKNAME = "nickname"
        private const val KEY_EQUIPPED_TITLE = "equipped_title"
        private const val KEY_DAILY_HISTORY = "daily_history"
    }

    private fun loadDailyHistory(): Map<String, Int> {
        val raw = prefs.getString(KEY_DAILY_HISTORY, "").orEmpty()
        if (raw.isBlank()) return emptyMap()

        return raw.split("|")
            .mapNotNull { entry ->
                val parts = entry.split(":")
                if (parts.size != 2) return@mapNotNull null
                val percent = parts[1].toIntOrNull() ?: return@mapNotNull null
                parts[0] to percent.coerceIn(0, 100)
            }
            .toMap()
    }

    private fun serializeDailyHistory(history: Map<String, Int>): String {
        return history.entries.joinToString("|") { "${it.key}:${it.value}" }
    }
}
