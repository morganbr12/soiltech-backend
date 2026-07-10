package com.soiltech.backend.interfaces.controller

import com.soiltech.backend.application.dto.customer.*
import com.soiltech.backend.application.dto.logistics.DispatchDriverRequest
import com.soiltech.backend.application.dto.logistics.DriverDispatchDto
import com.soiltech.backend.application.dto.order.CustomerOrderDto
import com.soiltech.backend.application.dto.order.CustomerOrderListDto
import com.soiltech.backend.application.dto.order.UpdateOrderStatusRequest
import com.soiltech.backend.application.mapper.toListDto
import com.soiltech.backend.application.usecase.customer.CreateProduceOrderUseCase
import com.soiltech.backend.application.usecase.logistics.AdminDispatchDriverUseCase
import com.soiltech.backend.application.usecase.logistics.AgentFieldConfirmUseCase
import com.soiltech.backend.application.usecase.order.UpdateOrderStatusUseCase
import com.soiltech.backend.domain.enum.OrderStatus
import com.soiltech.backend.domain.repository.CustomerOrderRepository
import com.soiltech.backend.infrastructure.security.UserPrincipal
import com.soiltech.backend.interfaces.response.ApiResponse
import com.soiltech.backend.interfaces.response.PaginationMeta
import jakarta.validation.Valid
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.util.UUID

@RestController
@RequestMapping("/customers/orders")
class CustomerOrderAdminController(
    private val customerOrderRepository: CustomerOrderRepository,
    private val createProduceOrderUseCase: CreateProduceOrderUseCase,
    private val updateOrderStatusUseCase: UpdateOrderStatusUseCase,
    private val agentFieldConfirmUseCase: AgentFieldConfirmUseCase,
    private val adminDispatchDriverUseCase: AdminDispatchDriverUseCase
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

    @PostMapping(consumes = ["multipart/form-data", "application/x-www-form-urlencoded", "application/json"])
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('customers:orders')")
    fun create(
        @RequestParam(name = "customer_id", required = false) customerId: UUID?,
        @RequestParam(name = "customerId", required = false) customerIdCamel: UUID?,
        @RequestParam produce: String,
        @RequestParam(name = "quantity_kg", required = false) quantityKg: Double?,
        @RequestParam(name = "quantityKg", required = false) quantityKgCamel: Double?,
        @RequestParam(name = "price_per_kg", required = false) pricePerKg: BigDecimal?,
        @RequestParam(name = "pricePerKg", required = false) pricePerKgCamel: BigDecimal?,
        @RequestParam region: String,
        @RequestParam(name = "assigned_agent", required = false) assignedAgent: String?,
        @RequestParam(name = "assignedAgent", required = false) assignedAgentCamel: String?
    ): ResponseEntity<ApiResponse<ProduceOrderResponse>> {
        val request = CreateProduceOrderRequest(
            customerId = customerId ?: customerIdCamel
                ?: throw com.soiltech.backend.interfaces.exception.BadRequestException("customer_id is required"),
            produce = produce,
            quantityKg = quantityKg ?: quantityKgCamel
                ?: throw com.soiltech.backend.interfaces.exception.BadRequestException("quantity_kg is required"),
            pricePerKg = pricePerKg ?: pricePerKgCamel
                ?: throw com.soiltech.backend.interfaces.exception.BadRequestException("price_per_kg is required"),
            region = region,
            assignedAgent = assignedAgent ?: assignedAgentCamel
        )
        val data = createProduceOrderUseCase.execute(request)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created(data, "Order created successfully"))
    }

    @PatchMapping("/{orderId}/confirm")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('customers:orders')")
    fun confirm(
        @PathVariable orderId: UUID,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ApiResponse<CustomerOrderDto>> {
        val data = updateOrderStatusUseCase.execute(orderId, UpdateOrderStatusRequest(OrderStatus.CONFIRMED), principal.id)
        return ResponseEntity.ok(ApiResponse.success(data, "Order confirmed"))
    }

    @PatchMapping("/{orderId}/cancel")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('customers:orders')")
    fun cancel(
        @PathVariable orderId: UUID,
        @RequestBody(required = false) request: CancelOrderRequest?,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ApiResponse<CustomerOrderDto>> {
        val data = updateOrderStatusUseCase.execute(
            orderId,
            UpdateOrderStatusRequest(OrderStatus.CANCELLED, request?.reason),
            principal.id
        )
        return ResponseEntity.ok(ApiResponse.success(data, "Order cancelled"))
    }

    @PatchMapping("/{orderId}/deliver")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('customers:orders')")
    fun deliver(
        @PathVariable orderId: UUID,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ApiResponse<CustomerOrderDto>> {
        val data = updateOrderStatusUseCase.execute(orderId, UpdateOrderStatusRequest(OrderStatus.DELIVERED), principal.id)
        return ResponseEntity.ok(ApiResponse.success(data, "Order marked as delivered"))
    }

    @PatchMapping("/{orderId}/field-confirm")
    @PreAuthorize("hasRole('AGENT') or hasRole('ADMIN')")
    fun fieldConfirm(
        @PathVariable orderId: UUID,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ApiResponse<CustomerOrderDto>> {
        val data = agentFieldConfirmUseCase.execute(orderId, principal.id)
        return ResponseEntity.ok(ApiResponse.success(data, "Order confirmed as ready at farm"))
    }

    @PostMapping("/{orderId}/dispatch-driver")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('customers:orders')")
    fun dispatchDriver(
        @PathVariable orderId: UUID,
        @Valid @RequestBody request: DispatchDriverRequest,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ApiResponse<DriverDispatchDto>> {
        val data = adminDispatchDriverUseCase.execute(orderId, request, principal.id)
        return ResponseEntity.ok(ApiResponse.success(data, "Driver dispatched successfully"))
    }
}
