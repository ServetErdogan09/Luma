package com.serveterdogan.lume.data.repo

import com.serveterdogan.lume.data.local.dao.DailySummaryDao
import com.serveterdogan.lume.data.mapper.toDomain
import com.serveterdogan.lume.data.mapper.toEntity
import com.serveterdogan.lume.domain.model.DailySummary
import com.serveterdogan.lume.domain.repository.DailySummaryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class DailySummaryRepositoryImpl @Inject constructor(
    private val dao : DailySummaryDao
) : DailySummaryRepository{
    override suspend fun insertDailySummary(dailySummary: DailySummary) {
      dao.insertDailySummary(dailySummary.toEntity())
    }
    override suspend fun getSummaryByDate(date: LocalDate): List<DailySummary> {
        return dao.getSummaryByDate(date = date).map { dailySummaryEntity ->
            dailySummaryEntity.toDomain()
        }
    }

    override fun getAllSummaries(): Flow<List<DailySummary>> {
        return dao.getAllSummaries().map { list ->
            list.map { it.toDomain() }
        }
    }
    
    override suspend fun deleteOldSummaries(today: LocalDate) {
        dao.deleteOldSummaries(today)
    }
}