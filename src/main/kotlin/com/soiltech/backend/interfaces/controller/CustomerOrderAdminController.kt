package com.soiltech.backend.interfaces.controller

import com.soiltech.backend.application.dto.customer.*
import com.soiltech.backend.application.usecase.customer.*
import com.soiltech.backend.domain.enum.ProduceOrderStatus
import com.soiltech.backend.domain.enum.ProducePaymentStatus
import com.soiltech.backend.interfaces.response.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/customers/orders")
class CustomerOrderAdminController(
    private val listProduceOrdersUseCase: ListProduceOrdersUseCase,
    private val createProduceOrderUseCase: CreateProduceOrderUseCase,
    private val confirmOrderUseCase: ConfirmOrderUseCase,
    private val cancelOrderUseCase: CancelOrderUseCase,
    private val deliverOrderUseCase: DeliverOrderUseCase
) {

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('customers:orders')")
    fun list(
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "20") limit: Int,
        @RequestParam(required = false) status: String?,
        @RequestParam(required = false) paymentStatus: String?,
        @RequestParam(required = false) region: String?,
        @RequestParam(required = false) customerId: UUID?,
        @RequestParam(required = false) search: String?,
        @RequestParam(name = "sortBy", defaultValue = "createdAt") sortBy: String,
        @RequestParam(name = "sortOrder", defaultValue = "desc") sortOrder: String
    ): ResponseEntity<ApiResponse<List<ProduceOrderResponse>>> {
        val orderStatus = status?.let { ProduceOrderStatus.fromValue(it) }
        val payStatus = paymentStatus?.let { ProducePaymentStatus.fromValue(it) }
        val (items, summary, meta) = listProduceOrdersUseCase.execute(
            orderStatus, payStatus, region, customerId, search, page, limit, sortBy, sortOrder
        )
        return ResponseEntity.ok(ApiResponse.success(items, meta = meta, summary = summary))
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('customers:orders')")
    fun create(
        @Valid @RequestBody request: CreateProduceOrderRequest
    ): ResponseEntity<ApiResponse<ProduceOrderResponse>> {
        val data = createProduceOrderUseCase.execute(request)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created(data, "Order created successfully"))
    }

    @PatchMapping("/{orderId}/confirm")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('customers:orders')")
    fun confirm(@PathVariable orderId: UUID): ResponseEntity<ApiResponse<ProduceOrderResponse>> {
        val data = confirmOrderUseCase.execute(orderId)
        return ResponseEntity.ok(ApiResponse.success(data, "Order confirmed"))
    }

    @PatchMapping("/{orderId}/cancel")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('customers:orders')")
    fun cancel(
        @PathVariable orderId: UUID,
        @RequestBody(required = false) request: CancelOrderRequest?
    ): ResponseEntity<ApiResponse<ProduceOrderResponse>> {
        val data = cancelOrderUseCase.execute(orderId, request ?: CancelOrderRequest())
        return ResponseEntity.ok(ApiResponse.success(data, "Order cancelled"))
    }

    @PatchMapping("/{orderId}/deliver")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('customers:orders')")
    fun deliver(@PathVariable orderId: UUID): ResponseEntity<ApiResponse<ProduceOrderResponse>> {
        val data = deliverOrderUseCase.execute(orderId)
        return ResponseEntity.ok(ApiResponse.success(data, "Order marked as delivered"))
    }
}
