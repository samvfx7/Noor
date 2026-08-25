package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.DhikrItem
import com.example.data.model.PrayerRecordEntity
import com.example.data.model.QuranBookmarkEntity
import com.example.data.model.TasbihSessionEntity

@Database(
    entities = [
        PrayerRecordEntity::class,
        DhikrItem::class,
        QuranBookmarkEntity::class,
        TasbihSessionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun prayerRecordDao(): PrayerRecordDao
    abstract fun dhikrDao(): DhikrDao
    abstract fun quranBookmarkDao(): QuranBookmarkDao
    abstract fun tasbihSessionDao(): TasbihSessionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "noor_prayer_database.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
