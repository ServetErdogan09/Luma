package com.serveterdogan.lume.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.serveterdogan.lume.data.local.converter.DataConverter
import com.serveterdogan.lume.data.local.dao.DailySummaryDao
import com.serveterdogan.lume.data.local.dao.MemoryDao
import com.serveterdogan.lume.data.local.entity.DailySummaryEntity
import com.serveterdogan.lume.data.local.entity.MemoryEntity

@Database(
    entities = [MemoryEntity::class , DailySummaryEntity::class] ,
    version = 1
)
@TypeConverters(DataConverter::class)
abstract class LumaDatabase : RoomDatabase() {
    abstract fun memoryDao(): MemoryDao
    abstract fun dailySummaryDao() : DailySummaryDao
}