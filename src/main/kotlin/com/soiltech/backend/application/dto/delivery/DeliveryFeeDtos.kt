package com.soiltech.backend.application.dto.delivery

import java.math.BigDecimal
import java.util.UUID

data class DeliveryFeeResponse(
    val productId: UUID,
    val distanceKm: Double?,
    val feeGhs: BigDecimal,
    val breakdown: DeliveryFeeBreakdown
)

data class DeliveryFeeBreakdown(
    val baseFee: BigDecimal,
    val distanceFee: BigDecimal,
    val distanceKm: Double?,
    val ratePerKm: BigDecimal,
    val method: String
)
