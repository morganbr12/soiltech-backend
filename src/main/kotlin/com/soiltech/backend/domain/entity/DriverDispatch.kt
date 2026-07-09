package com.soiltech.backend.domain.entity

import com.soiltech.backend.domain.enum.DispatchStatus
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class DriverDispatch(
    val id: UUID,
    val orderId: UUID,
    val customerName: String?,
    val vehicleId: UUID,
    val driverName: String,
    val plateNumber: String,
    val vehicleType: String,
    val scheduledDate: LocalDate,
    val pickupLocation: String?,
    val notes: String?,
    val status: DispatchStatus,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val createdBy: UUID?,
    val updatedBy: UUID?
)
