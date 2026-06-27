package com.serveterdogan.lume.domain.model

import java.time.LocalDate

data class DailySummary(
    val id : Int,
    val date : LocalDate,
    val aiStory : String,
    val isCreated : Boolean = false
)