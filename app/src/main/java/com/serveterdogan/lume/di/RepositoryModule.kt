package com.serveterdogan.lume.di // veya com.serveterdogan.lume.data.di

import com.serveterdogan.lume.data.repo.DailySummaryRepositoryImpl
import com.serveterdogan.lume.data.repo.MemoryRepositoryImpl
import com.serveterdogan.lume.domain.repository.DailySummaryRepository
import com.serveterdogan.lume.domain.repository.MemoryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMemoryRepository(
        memoryRepositoryImpl: MemoryRepositoryImpl
    ): MemoryRepository

    @Binds
    @Singleton
    abstract fun bindDailySummaryRepository(
        dailySummaryImpl: DailySummaryRepositoryImpl
    ): DailySummaryRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        settingsRepositoryImpl: com.serveterdogan.lume.data.repo.SettingsRepositoryImpl
    ): com.serveterdogan.lume.domain.repository.SettingsRepository
}