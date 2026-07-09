package com.soiltech.backend.application.dto.order

import com.soiltech.backend.domain.enum.OrderStatus
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class OrderItemDto(
    val id: UUID,
    val productId: UUID,
    val productName: String?,
    val agentName: String?,
    val region: String?,
    val quantity: Int,
    val unitPrice: BigDecimal,
    val subtotal: BigDecimal
)

data class OrderTimelineDto(
    val id: UUID,
    val status: OrderStatus,
    val note: String?,
    val createdAt: LocalDateTime,
    val createdBy: UUID?
)

data class CustomerOrderDto(
    val id: UUID,
    val customerId: UUID,
    val customerName: String?,
    val status: OrderStatus,
    val totalAmount: BigDecimal,
    val deliveryAddress: String,
    val paymentType: String?,
    val notes: String?,
    val items: List<OrderItemDto>,
    val timeline: List<OrderTimelineDto>,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class CustomerOrderListDto(
    val id: UUID,
    val customerId: UUID,
    val customerName: String?,
    val status: OrderStatus,
    val totalAmount: BigDecimal,
    val deliveryAddress: String,
    val paymentType: String?,
    val itemCount: Int,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class OrderItemRequest(
    @field:NotNull
    val productId: UUID,
    @field:Min(1)
    val quantity: Int
)

data class PlaceOrderRequest(
    @field:NotBlank
    val deliveryAddress: String,
    @field:NotEmpty @field:Valid
    val items: List<OrderItemRequest>,
    val paymentType: String? = null,
    val notes: String? = null
)

data class UpdateOrderStatusRequest(
    @field:NotNull
    val status: OrderStatus,
    val note: String? = null
)
