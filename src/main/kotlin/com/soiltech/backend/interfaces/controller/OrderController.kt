package com.soiltech.backend.interfaces.controller

import com.soiltech.backend.application.dto.order.CustomerOrderDto
import com.soiltech.backend.application.dto.order.CustomerOrderListDto
import com.soiltech.backend.application.dto.order.PlaceOrderRequest
import com.soiltech.backend.application.dto.order.UpdateOrderStatusRequest
import com.soiltech.backend.application.usecase.order.*
import com.soiltech.backend.domain.enum.OrderStatus
import com.soiltech.backend.infrastructure.security.UserPrincipal
import com.soiltech.backend.interfaces.response.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/orders")
class OrderController(
    private val placeOrderUseCase: PlaceOrderUseCase,
    private val listOrdersUseCase: ListOrdersUseCase,
    private val getOrderUseCase: GetOrderUseCase,
    private val updateOrderStatusUseCase: UpdateOrderStatusUseCase
) {

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    fun placeOrder(
        @Valid @RequestBody request: PlaceOrderRequest,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ApiResponse<CustomerOrderDto>> {
        val data = placeOrderUseCase.execute(request, principal.id)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created(data, "Order placed successfully"))
    }

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    fun list(
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(name = "per_page", defaultValue = "20") perPage: Int,
        @RequestParam(required = false) status: String?,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ApiResponse<List<CustomerOrderListDto>>> {
        val statuses = when (status?.lowercase()) {
            null -> null
            "active" -> listOf(OrderStatus.CONFIRMED, OrderStatus.PROCESSING, OrderStatus.SHIPPED)
            else -> listOf(OrderStatus.fromValue(status))
        }
        val (orders, meta) = listOrdersUseCase.execute(principal.id, statuses, page, perPage)
        return ResponseEntity.ok(ApiResponse.success(orders, meta = meta))
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    fun getById(
        @PathVariable id: UUID,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ApiResponse<CustomerOrderDto>> {
        val data = getOrderUseCase.execute(id, principal.id)
        return ResponseEntity.ok(ApiResponse.success(data))
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    fun updateStatus(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateOrderStatusRequest,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ApiResponse<CustomerOrderDto>> {
        val data = updateOrderStatusUseCase.execute(id, request, principal.id)
        return ResponseEntity.ok(ApiResponse.success(data, "Order status updated"))
    }
}
