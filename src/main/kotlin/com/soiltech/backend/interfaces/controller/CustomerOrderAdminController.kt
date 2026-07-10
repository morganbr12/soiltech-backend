package com.soiltech.backend.interfaces.controller

import com.soiltech.backend.application.dto.customer.*
import com.soiltech.backend.application.dto.logistics.DispatchDriverRequest
import com.soiltech.backend.application.dto.logistics.DriverDispatchDto
import com.soiltech.backend.application.usecase.customer.CancelOrderUseCase
import com.soiltech.backend.application.usecase.customer.ConfirmOrderUseCase
import com.soiltech.backend.application.usecase.customer.CreateProduceOrderUseCase
import com.soiltech.backend.application.usecase.customer.DeliverOrderUseCase
import com.soiltech.backend.application.usecase.logistics.AdminDispatchDriverUseCase
import com.soiltech.backend.application.usecase.logistics.AgentFieldConfirmUseCase
import com.soiltech.backend.domain.enum.ProduceOrderStatus
import com.soiltech.backend.domain.repository.AgentRepository
import com.soiltech.backend.domain.repository.CustomerProduceOrderRepository
import com.soiltech.backend.domain.repository.CustomerProfileRepository
import com.soiltech.backend.domain.repository.FarmerRepository
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
import java.util.UUID

@RestController
@RequestMapping("/customers/orders")
class CustomerOrderAdminController(
    private val customerProduceOrderRepository: CustomerProduceOrderRepository,
    private val customerProfileRepository: CustomerProfileRepository,
    private val farmerRepository: FarmerRepository,
    private val agentRepository: AgentRepository,
    private val createProduceOrderUseCase: CreateProduceOrderUseCase,
    private val confirmOrderUseCase: ConfirmOrderUseCase,
    private val cancelOrderUseCase: CancelOrderUseCase,
    private val deliverOrderUseCase: DeliverOrderUseCase,
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
    ): ResponseEntity<ApiResponse<List<ProduceOrderResponse>>> {
        val produceStatus = status?.takeIf { it.isNotBlank() }?.let {
            runCatching { ProduceOrderStatus.fromValue(it) }.getOrNull()
        }
        val direction = if (sortOrder.equals("asc", ignoreCase = true)) Sort.Direction.ASC else Sort.Direction.DESC
        val pageable = PageRequest.of((page - 1).coerceAtLeast(0), limit, Sort.by(direction, "createdAt"))
        val result = customerProduceOrderRepository.findAll(produceStatus, null, null, customerId, null, pageable)
        val orders = result.content
        val customerIds = orders.map { it.customerId }.distinct()
        val farmerIds = orders.mapNotNull { it.farmerId }.distinct()
        val agentIds = orders.mapNotNull { it.agentId }.distinct()
        val customerMap = customerProfileRepository.findByIds(customerIds)
        val farmerMap = farmerRepository.findByIds(farmerIds).associateBy { it.id }
        val agentMap = agentRepository.findByIds(agentIds)
        val dtos = orders.map { order ->
            val profile = customerMap[order.customerId]
            val farmer = farmerMap[order.farmerId]
            val agent = agentMap[order.agentId]
            ProduceOrderResponse(
                id = order.id, orderCode = order.orderCode, customerId = order.customerId,
                customerCode = order.customerCode, customerName = order.customerName,
                customer = profile?.let {
                    CustomerSummary(
                        id = it.id, customerCode = it.customerCode, fullName = it.fullName,
                        email = it.email, phone = it.phone, address = it.address,
                        region = it.region, accountType = it.accountType, status = it.status
                    )
                },
                farmer = farmer?.let {
                    FarmerSummary(
                        id = it.id, farmerCode = it.farmerCode,
                        fullName = "${it.firstName} ${it.lastName}",
                        phone = it.phone, email = it.email,
                        region = it.region, district = it.district,
                        community = it.community, cropTypes = it.cropTypes
                    )
                },
                agent = agent?.let {
                    AgentSummary(
                        id = it.id, agentCode = it.agentCode,
                        fullName = "${it.firstName} ${it.lastName}",
                        phone = it.phone, email = it.email,
                        region = it.region, district = it.district
                    )
                },
                produce = order.produce, quantityKg = order.quantityKg,
                pricePerKg = order.pricePerKg, totalAmount = order.totalAmount, status = order.status,
                paymentStatus = order.paymentStatus, assignedAgent = order.assignedAgent,
                assignedDriver = order.assignedDriver, orderDate = order.orderDate,
                deliveryDate = order.deliveryDate, region = order.region,
                cancellationReason = order.cancellationReason,
                farmerName = order.farmerName, farmerPhone = order.farmerPhone, agentPhone = order.agentPhone,
                createdAt = order.createdAt, updatedAt = order.updatedAt
            )
        }
        return ResponseEntity.ok(ApiResponse.success(dtos, meta = PaginationMeta.from(result, page, limit)))
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('customers:orders') or hasRole('CUSTOMER')")
    fun create(
        @Valid @RequestBody request: CreateProduceOrderRequest,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ApiResponse<ProduceOrderResponse>> {
        val data = createProduceOrderUseCase.execute(request, principal.id)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created(data, "Order created successfully"))
    }

    @PatchMapping("/{orderId}/confirm")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('customers:orders')")
    fun confirm(
        @PathVariable orderId: UUID,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ApiResponse<ProduceOrderResponse>> {
        val data = confirmOrderUseCase.execute(orderId)
        return ResponseEntity.ok(ApiResponse.success(data, "Order confirmed"))
    }

    @PatchMapping("/{orderId}/cancel")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('customers:orders')")
    fun cancel(
        @PathVariable orderId: UUID,
        @RequestBody(required = false) request: CancelOrderRequest?,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ApiResponse<ProduceOrderResponse>> {
        val data = cancelOrderUseCase.execute(orderId, request ?: CancelOrderRequest())
        return ResponseEntity.ok(ApiResponse.success(data, "Order cancelled"))
    }

    @PatchMapping("/{orderId}/deliver")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('customers:orders')")
    fun deliver(
        @PathVariable orderId: UUID,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ApiResponse<ProduceOrderResponse>> {
        val data = deliverOrderUseCase.execute(orderId)
        return ResponseEntity.ok(ApiResponse.success(data, "Order marked as delivered"))
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
