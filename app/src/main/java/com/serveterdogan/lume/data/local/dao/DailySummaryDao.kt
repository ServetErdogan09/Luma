package com.serveterdogan.lume.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.serveterdogan.lume.data.local.entity.DailySummaryEntity
import com.serveterdogan.lume.domain.model.DailySummary
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface DailySummaryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailySummary(dailySummaryEntity: DailySummaryEntity)

    @Query("SELECT * FROM daily_summary WHERE date = :date")
    suspend fun getSummariesByDateRange(date : LocalDate): List<DailySummaryEntity>

    @Query("SELECT * FROM daily_summary ORDER BY date DESC")
    fun getAllSummaries(): Flow<List<DailySummaryEntity>>
}