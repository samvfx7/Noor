package com.samvfx7.noor.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.samvfx7.noor.data.model.QuranBookmarkEntity
import com.samvfx7.noor.data.model.TasbihSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuranBookmarkDao {
    @Query("SELECT * FROM quran_bookmarks ORDER BY createdAt DESC")
    fun getAllBookmarks(): Flow<List<QuranBookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: QuranBookmarkEntity): Long

    @Query("DELETE FROM quran_bookmarks WHERE surahNumber = :surahNumber AND ayahNumber = :ayahNumber")
    suspend fun deleteBookmark(surahNumber: Int, ayahNumber: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM quran_bookmarks WHERE surahNumber = :surahNumber AND ayahNumber = :ayahNumber)")
    suspend fun isBookmarked(surahNumber: Int, ayahNumber: Int): Boolean
}

@Dao
interface TasbihSessionDao {
    @Query("SELECT * FROM tasbih_sessions ORDER BY timestamp DESC LIMIT 50")
    fun getRecentSessions(): Flow<List<TasbihSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: TasbihSessionEntity): Long
}
