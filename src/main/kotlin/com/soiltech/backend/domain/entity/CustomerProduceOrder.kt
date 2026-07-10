package com.soiltech.backend.domain.entity

import com.soiltech.backend.domain.enum.ProduceOrderStatus
import com.soiltech.backend.domain.enum.ProducePaymentStatus
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class CustomerProduceOrder(
    val id: UUID,
    val orderCode: String,
    val customerId: UUID,
    val customerCode: String,
    val customerName: String,
    val farmerId: UUID? = null,
    val agentId: UUID? = null,
    val produce: String,
    val quantityKg: Double,
    val pricePerKg: BigDecimal,
    val totalAmount: BigDecimal,
    val status: ProduceOrderStatus,
    val paymentStatus: ProducePaymentStatus,
    val assignedAgent: String?,
    val assignedDriver: String?,
    val region: String,
    val cancellationReason: String?,
    val orderDate: LocalDate,
    val deliveryDate: LocalDate?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val farmerName: String? = null,
    val farmerPhone: String? = null,
    val agentPhone: String? = null
)
