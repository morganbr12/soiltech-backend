package com.soiltech.backend.domain.entity

import java.time.LocalDateTime
import java.util.UUID

data class VehicleLocation(
    val id: UUID,
    val vehicleId: UUID,
    val lat: Double,
    val lng: Double,
    val speed: Double,
    val heading: Double,
    val lastUpdated: LocalDateTime
)
