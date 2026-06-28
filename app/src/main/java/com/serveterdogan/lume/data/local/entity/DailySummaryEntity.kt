package com.serveterdogan.lume.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "daily_summary" , indices = [Index(value = ["date"])])
data class DailySummaryEntity(
    @PrimaryKey(autoGenerate = true)
    val id : Int = 0,
    val date : LocalDate,
    val aiStory : String,
    val isCreated : Boolean = false
)
