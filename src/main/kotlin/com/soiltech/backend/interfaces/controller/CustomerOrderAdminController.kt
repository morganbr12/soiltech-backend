package com.soiltech.backend.interfaces.controller

import com.soiltech.backend.application.dto.customer.*
import com.soiltech.backend.application.dto.order.CustomerOrderListDto
import com.soiltech.backend.application.mapper.toListDto
import com.soiltech.backend.application.usecase.customer.*
import com.soiltech.backend.domain.enum.OrderStatus
import com.soiltech.backend.domain.enum.ProduceOrderStatus
import com.soiltech.backend.domain.enum.ProducePaymentStatus
import com.soiltech.backend.domain.repository.CustomerOrderRepository
import com.soiltech.backend.interfaces.response.ApiResponse
import com.soiltech.backend.interfaces.response.PaginationMeta
import jakarta.validation.Valid
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/customers/orders")
class CustomerOrderAdminController(
    private val customerOrderRepository: CustomerOrderRepository,
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
        @RequestParam(required = false) customerId: UUID?,
        @RequestParam(name = "sortOrder", defaultValue = "desc") sortOrder: String
    ): ResponseEntity<ApiResponse<List<CustomerOrderListDto>>> {
        val statuses = status?.takeIf { it.isNotBlank() }?.let {
            listOf(OrderStatus.fromValue(it))
        }
        val direction = if (sortOrder.equals("asc", ignoreCase = true)) Sort.Direction.ASC else Sort.Direction.DESC
        val pageable = PageRequest.of((page - 1).coerceAtLeast(0), limit, Sort.by(direction, "createdAt"))
        val result = customerOrderRepository.findAllAdmin(customerId, statuses, pageable)
        val dtos = result.content.map { order ->
            val items = customerOrderRepository.findItemsByOrderId(order.id)
            order.toListDto(items.size)
        }
        return ResponseEntity.ok(ApiResponse.success(dtos, meta = PaginationMeta.from(result, page, limit)))
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
