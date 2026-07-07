package com.serveterdogan.lume.domain.repository

import com.serveterdogan.lume.domain.model.Memory
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface MemoryRepository {

    suspend fun insertMemory(memory: Memory)

    suspend fun updateMemory(memory: Memory)

    suspend fun getMemoriesByDate(date : LocalDate): List<Memory>

    fun getMemoriesByDateFlow(date: LocalDate): Flow<List<Memory>>

    fun getAllMemories(): Flow<List<Memory>>

    suspend fun getMemoryById(id: Int): Memory?

    suspend fun deleteMemoryById(id: Int)

    suspend fun deleteAllMemories()

    suspend fun searchMemories(searchQuery: String): List<Memory>

    suspend fun getSimilarMemories(tags: List<String>, excludeId: Int): List<Memory>

    suspend fun getAllSimilarMemories(tags: List<String>, excludeId: Int): List<Memory>
}