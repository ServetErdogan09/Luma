package com.serveterdogan.lume.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "daily_summary")
data class DailySummaryEntity(
    @PrimaryKey(autoGenerate = true)
    val id : Int,
    val date : LocalDate,
    val aiStory : String,
    val isCreated : Boolean = false
)
