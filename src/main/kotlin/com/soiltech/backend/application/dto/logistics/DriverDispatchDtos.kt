package com.soiltech.backend.application.dto.logistics

import com.soiltech.backend.domain.enum.DispatchStatus
import jakarta.validation.constraints.Future
import jakarta.validation.constraints.NotNull
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class DriverDispatchDto(
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
    val updatedAt: LocalDateTime
)

data class DispatchDriverRequest(
    @field:NotNull val vehicleId: UUID,
    @field:NotNull @field:Future val scheduledDate: LocalDate,
    val pickupLocation: String? = null,
    val notes: String? = null
)

data class UpdateDispatchStatusRequest(
    @field:NotNull val status: DispatchStatus
)
