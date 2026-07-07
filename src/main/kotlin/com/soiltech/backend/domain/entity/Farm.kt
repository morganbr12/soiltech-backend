package com.soiltech.backend.domain.entity

import java.time.LocalDateTime
import java.util.UUID

data class Farm(
    val id: UUID,
    val farmerId: UUID,
    val name: String,
    val sizeHectares: Double?,
    val cropType: String?,
    val location: String?,
    val latitude: Double?,
    val longitude: Double?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
