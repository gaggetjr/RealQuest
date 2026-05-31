package com.example.questmvp.data

import android.content.Context
import androidx.room.Database
import androidx.room.migration.Migration
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Quest::class], version = 2, exportSchema = false)
@TypeConverters(QuestConverters::class)
abstract class QuestDatabase : RoomDatabase() {
    abstract fun questDao(): QuestDao

    companion object {
        @Volatile
        private var instance: QuestDatabase? = null

        private val migration1To2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE quests ADD COLUMN lastRewardedPeriodKey TEXT")
            }
        }

        fun getInstance(context: Context): QuestDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    QuestDatabase::class.java,
                    "quest_mvp.db"
                )
                    .addMigrations(migration1To2)
                    .build()
                    .also { instance = it }
            }
        }
    }
}
