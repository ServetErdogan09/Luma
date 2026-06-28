package com.serveterdogan.lume.data.local.converter

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.chrono.ChronoLocalDate

class DataConverter {


    @RequiresApi(Build.VERSION_CODES.O)
    @TypeConverter
    fun fromLocalDate(date : LocalDate?) : String?{
        return date?.toString()
    }


    @RequiresApi(Build.VERSION_CODES.O)
    @TypeConverter
    fun toLocalDate(dateString : String?) : LocalDate?{
        return dateString?.let { LocalDate.parse(it) }
    }
}