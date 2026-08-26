package com.samvfx7.noor.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.samvfx7.noor.data.model.DhikrItem
import kotlinx.coroutines.flow.Flow

@Dao
interface DhikrDao {
    @Query("SELECT * FROM custom_dhikr ORDER BY id ASC")
    fun getAllCustomDhikrs(): Flow<List<DhikrItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDhikr(item: DhikrItem): Long

    @Update
    suspend fun updateDhikr(item: DhikrItem)

    @Query("DELETE FROM custom_dhikr WHERE id = :id")
    suspend fun deleteDhikrById(id: Long)
}
