package com.serveterdogan.lume.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "memories" , indices = [Index(value = ["date"])])
data class MemoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id : Int,
    val imagePath : String,
    val date  : LocalDate,
    val time : String,
    val tag : String,
    val title: String,
    val aiDescription : String

)