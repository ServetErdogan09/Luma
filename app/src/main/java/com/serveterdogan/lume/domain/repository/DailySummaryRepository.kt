package com.serveterdogan.lume.domain.repository

import com.serveterdogan.lume.domain.model.DailySummary
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface DailySummaryRepository {

    suspend fun insertDailySummary(dailySummary: DailySummary)

    suspend fun getSummariesByDateRange(date : LocalDate): List<DailySummary>

    fun getAllSummaries(): Flow<List<DailySummary>>

}