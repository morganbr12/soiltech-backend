package com.soiltech.backend.application.usecase.delivery

import com.soiltech.backend.application.dto.delivery.DeliveryFeeBreakdown
import com.soiltech.backend.application.dto.delivery.DeliveryFeeResponse
import com.soiltech.backend.domain.repository.FarmRepository
import com.soiltech.backend.domain.repository.ProduceListingRepository
import com.soiltech.backend.domain.repository.ProductRepository
import com.soiltech.backend.interfaces.exception.NotFoundException
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.UUID
import kotlin.math.*

@Service
class CalculateDeliveryFeeUseCase(
    private val productRepository: ProductRepository,
    private val produceListingRepository: ProduceListingRepository,
    private val farmRepository: FarmRepository
) {
    companion object {
        private val BASE_FEE = BigDecimal("15.00")
        private val RATE_PER_KM = BigDecimal("1.50")
        private val MIN_FEE = BigDecimal("15.00")
        private val MAX_FEE = BigDecimal("300.00")
        private val ZONE_FEE = BigDecimal("50.00")
    }

    fun execute(
        productId: UUID,
        deliveryLat: Double,
        deliveryLng: Double,
        fromLat: Double? = null,
        fromLng: Double? = null
    ): DeliveryFeeResponse {
        val product = productRepository.findById(productId)
            ?: throw NotFoundException("Product not found")

        val sourceLat: Double?
        val sourceLng: Double?

        if (fromLat != null && fromLng != null) {
            sourceLat = fromLat
            sourceLng = fromLng
        } else {
            val listing = product.produceListingId
                ?.let { produceListingRepository.findById(it) }
            val farm = listing?.farmId?.let { farmRepository.findById(it) }
            sourceLat = farm?.latitude
            sourceLng = farm?.longitude
        }

        return if (sourceLat != null && sourceLng != null) {
            byDistance(productId, sourceLat, sourceLng, deliveryLat, deliveryLng)
        } else {
            byZone(productId)
        }
    }

    private fun byDistance(
        productId: UUID,
        fromLat: Double, fromLng: Double,
        toLat: Double, toLng: Double
    ): DeliveryFeeResponse {
        val distanceKm = haversineKm(fromLat, fromLng, toLat, toLng)
        val distanceFee = BigDecimal(distanceKm)
            .multiply(RATE_PER_KM)
            .setScale(2, RoundingMode.HALF_UP)
        val total = (BASE_FEE + distanceFee)
            .max(MIN_FEE)
            .min(MAX_FEE)
            .setScale(2, RoundingMode.HALF_UP)

        return DeliveryFeeResponse(
            productId = productId,
            distanceKm = distanceKm.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toDouble(),
            feeGhs = total,
            breakdown = DeliveryFeeBreakdown(
                baseFee = BASE_FEE,
                distanceFee = distanceFee,
                distanceKm = distanceKm,
                ratePerKm = RATE_PER_KM,
                method = "distance"
            )
        )
    }

    private fun byZone(productId: UUID) = DeliveryFeeResponse(
        productId = productId,
        distanceKm = null,
        feeGhs = ZONE_FEE,
        breakdown = DeliveryFeeBreakdown(
            baseFee = ZONE_FEE,
            distanceFee = BigDecimal.ZERO,
            distanceKm = null,
            ratePerKm = RATE_PER_KM,
            method = "zone"
        )
    )

    private fun haversineKm(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val R = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLng / 2).pow(2)
        return R * 2 * atan2(sqrt(a), sqrt(1 - a))
    }
}
