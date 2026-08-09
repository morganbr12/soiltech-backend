package com.soiltech.backend.domain.entity

import java.time.LocalDateTime
import java.util.UUID

data class VehicleLocationHistory(
    val id: UUID,
    val vehicleId: UUID,
    val lat: Double,
    val lng: Double,
    val speed: Double,
    val heading: Double,
    val recordedAt: LocalDateTime
)
