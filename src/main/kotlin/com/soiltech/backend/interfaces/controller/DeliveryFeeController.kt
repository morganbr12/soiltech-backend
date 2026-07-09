package com.soiltech.backend.interfaces.controller

import com.soiltech.backend.application.dto.delivery.DeliveryFeeResponse
import com.soiltech.backend.application.usecase.delivery.CalculateDeliveryFeeUseCase
import com.soiltech.backend.interfaces.response.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/delivery-fee")
class DeliveryFeeController(
    private val calculateDeliveryFeeUseCase: CalculateDeliveryFeeUseCase
) {

    @GetMapping
    fun calculate(
        @RequestParam(name = "product_id") productId: UUID,
        @RequestParam(name = "delivery_lat") deliveryLat: Double,
        @RequestParam(name = "delivery_lng") deliveryLng: Double,
        @RequestParam(name = "from_lat", required = false) fromLat: Double?,
        @RequestParam(name = "from_lng", required = false) fromLng: Double?
    ): ResponseEntity<ApiResponse<DeliveryFeeResponse>> {
        val data = calculateDeliveryFeeUseCase.execute(
            productId, deliveryLat, deliveryLng, fromLat, fromLng
        )
        return ResponseEntity.ok(ApiResponse.success(data))
    }
}
