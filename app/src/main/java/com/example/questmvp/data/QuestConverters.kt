package com.example.questmvp.data

import androidx.room.TypeConverter

class QuestConverters {
    @TypeConverter
    fun fromQuestType(value: QuestType): String = value.name

    @TypeConverter
    fun toQuestType(value: String): QuestType = QuestType.valueOf(value)
}
