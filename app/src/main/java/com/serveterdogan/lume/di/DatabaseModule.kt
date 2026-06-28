package com.serveterdogan.lume.di

import android.content.Context
import androidx.room.Room
import com.serveterdogan.lume.data.local.dao.DailySummaryDao
import com.serveterdogan.lume.data.local.dao.MemoryDao
import com.serveterdogan.lume.data.local.db.LumaDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule  {


    @Singleton
    @Provides
    fun provideLumaDatabase(@ApplicationContext context: Context) : LumaDatabase {
        return Room.databaseBuilder(
            context = context,
            klass = LumaDatabase::class.java,
            name = "luma_db",
        ).build()
    }

    @Provides
    @Singleton
    fun provideMemoryDao(lumaDatabase: LumaDatabase) : MemoryDao {
        return lumaDatabase.memoryDao()
    }


    @Provides
    @Singleton
    fun provideDailySummaryDao(lumaDatabase: LumaDatabase) : DailySummaryDao {
        return lumaDatabase.dailySummaryDao()
    }

}