package com.serveterdogan.lume.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.serveterdogan.lume.data.local.entity.MemoryEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface MemoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: MemoryEntity)

    @Update
    suspend fun updateMemory(memory: MemoryEntity)

    @Query("SELECT * FROM memories WHERE date = :date")
    suspend fun getMemoriesByDateRange(date: LocalDate): List<MemoryEntity>

    @Query("SELECT * FROM memories ORDER BY date DESC")
    fun getAllMemories(): Flow<List<MemoryEntity>>

    @Query("SELECT * FROM memories WHERE id = :id")
    suspend fun getMemoryById(id: Int): MemoryEntity?

    @Query("DELETE FROM memories WHERE id = :id")
    suspend fun deleteMemoryById(id: Int)

    @Query("SELECT * FROM memories WHERE tag LIKE '%' || :searchQuery || '%' OR aiDescription LIKE '%' || :searchQuery || '%' ORDER BY date DESC")
    suspend fun searchMemories(searchQuery: String): List<MemoryEntity>
}