package com.serveterdogan.lume.domain.model

import java.time.LocalDate

data class Memory(
    val id : Int,
    val imagePath : String,
    val date  : LocalDate,
    val time : String,
    val tag : String,
    val aiDescription : String
)


