package com.samvfx7.noor.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.samvfx7.noor.data.model.PrayerRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PrayerRecordDao {
    @Query("SELECT * FROM prayer_records WHERE dateKey = :dateKey")
    fun getRecordsForDate(dateKey: String): Flow<List<PrayerRecordEntity>>

    @Query("SELECT * FROM prayer_records WHERE dateKey = :dateKey")
    suspend fun getRecordsForDateOnce(dateKey: String): List<PrayerRecordEntity>

    @Query("SELECT * FROM prayer_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<PrayerRecordEntity>>

    @Query("SELECT * FROM prayer_records WHERE status = 'QADA' OR status = 'MISSED'")
    fun getMissedOrQadaRecords(): Flow<List<PrayerRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(record: PrayerRecordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<PrayerRecordEntity>)

    @Query("DELETE FROM prayer_records WHERE dateKey = :dateKey AND prayerName = :prayerName")
    suspend fun deleteRecord(dateKey: String, prayerName: String)

    @Query("DELETE FROM prayer_records WHERE dateKey = :dateKey")
    suspend fun deleteRecordsForDate(dateKey: String)

    @Query("SELECT COUNT(*) FROM prayer_records WHERE dateKey = :dateKey AND status IN ('ON_TIME', 'IN_CONGREGATION', 'LATE', 'QADA')")
    suspend fun getCompletedCountForDate(dateKey: String): Int
}

