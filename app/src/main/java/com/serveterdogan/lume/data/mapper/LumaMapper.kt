package com.serveterdogan.lume.data.mapper

import com.serveterdogan.lume.data.local.entity.DailySummaryEntity
import com.serveterdogan.lume.data.local.entity.MemoryEntity
import com.serveterdogan.lume.domain.model.DailySummary
import com.serveterdogan.lume.domain.model.Memory


// veri tabanından oku

fun MemoryEntity.toDomain() : Memory{
    return Memory(
        id = id,
        imagePath = imagePath,
        title = title,
        aiDescription = aiDescription,
        time = time,
        tag = tag,
        date = date
    )
}


fun Memory.toEntity() : MemoryEntity{
    return MemoryEntity(
        id = id,
        imagePath = imagePath,
        title = title,
        aiDescription = aiDescription,
        time = time,
        tag = tag,
        date = date
    )
}


fun DailySummaryEntity.toDomain() : DailySummary{
    return DailySummary(
        id = id,
        date = date,
        aiStory = aiStory,
        isCreated = isCreated
    )
}


fun DailySummary.toEntity() : DailySummaryEntity{
    return DailySummaryEntity(
        id = id,
        date = date,
        aiStory = aiStory,
        isCreated = isCreated
    )
}

