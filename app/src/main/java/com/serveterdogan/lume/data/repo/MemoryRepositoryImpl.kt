package com.serveterdogan.lume.data.repo

import com.serveterdogan.lume.data.local.dao.MemoryDao
import com.serveterdogan.lume.data.mapper.toDomain
import com.serveterdogan.lume.data.mapper.toEntity
import com.serveterdogan.lume.domain.model.Memory
import com.serveterdogan.lume.domain.repository.MemoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject


class MemoryRepositoryImpl @Inject constructor (
    private val  dao: MemoryDao
) : MemoryRepository{

    override suspend fun insertMemory(memory: Memory) {
        dao.insertMemory(memory.toEntity())
    }

    override suspend fun updateMemory(memory: Memory) {
        dao.updateMemory(memory.toEntity())
    }

    override suspend fun getMemoriesByDate(
        date : LocalDate
    ): List<Memory> {
       return dao.getMemoriesByDateRange(date).map { memoryEntity ->
           memoryEntity.toDomain()
       }

    }

    override fun getAllMemories(): Flow<List<Memory>> {
        return dao.getAllMemories().map { memoryEntities ->
            memoryEntities.map { entity ->
                entity.toDomain()
            }
        }
    }

    override suspend fun getMemoryById(id: Int): Memory? {
        return dao.getMemoryById(id)?.toDomain()
    }

    override suspend fun deleteMemoryById(id: Int) {
        dao.deleteMemoryById(id)
    }

    override suspend fun searchMemories(searchQuery: String): List<Memory> {
        return dao.searchMemories(searchQuery).map { entity ->
            entity.toDomain()
        }
    }


}