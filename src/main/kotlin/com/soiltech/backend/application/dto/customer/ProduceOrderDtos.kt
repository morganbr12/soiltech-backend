package com.soiltech.backend.application.dto.customer

import com.soiltech.backend.domain.enum.CustomerAccountType
import com.soiltech.backend.domain.enum.CustomerStatus
import com.soiltech.backend.domain.enum.ProduceOrderStatus
import com.soiltech.backend.domain.enum.ProducePaymentStatus
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class FarmerSummary(
    val id: UUID,
    val farmerCode: String,
    val fullName: String,
    val phone: String,
    val email: String?,
    val region: String,
    val district: String,
    val community: String,
    val cropTypes: List<String>
)

data class AgentSummary(
    val id: UUID,
    val agentCode: String,
    val fullName: String,
    val phone: String,
    val email: String,
    val region: String,
    val district: String
)

data class CustomerSummary(
    val id: UUID,
    val customerCode: String?,
    val fullName: String,
    val email: String?,
    val phone: String?,
    val address: String?,
    val region: String?,
    val accountType: CustomerAccountType,
    val status: CustomerStatus
)

data class ProduceOrderResponse(
    val id: UUID,
    val orderCode: String,
    val customerId: UUID,
    val customerCode: String,
    val customerName: String,
    val customer: CustomerSummary?,
    val farmer: FarmerSummary?,
    val agent: AgentSummary?,
    val produce: String,
    val quantityKg: Double,
    val pricePerKg: BigDecimal,
    val totalAmount: BigDecimal,
    val status: ProduceOrderStatus,
    val paymentStatus: ProducePaymentStatus,
    val assignedAgent: String?,
    val assignedDriver: String?,
    val orderDate: LocalDate,
    val deliveryDate: LocalDate?,
    val region: String,
    val cancellationReason: String?,
    val farmerName: String?,
    val farmerPhone: String?,
    val agentPhone: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class ProduceOrderSummaryResponse(
    val total: Long,
    val pending: Long,
    val confirmed: Long,
    val processing: Long,
    val delivered: Long,
    val cancelled: Long,
    val unpaid: Long,
    val totalValue: BigDecimal
)

data class CreateProduceOrderRequest(
    val customerId: UUID? = null,

    @field:NotBlank(message = "Produce is required")
    val produce: String,

    @field:DecimalMin("0.01", message = "Quantity must be positive")
    val quantityKg: Double,

    @field:NotNull
    @field:DecimalMin("0.01", message = "Price must be positive")
    val pricePerKg: BigDecimal,

    val region: String? = null,

    val assignedAgent: String? = null,

    val paymentType: String? = null,

    val farmerId: UUID? = null,

    val agentId: UUID? = null
)

data class CancelOrderRequest(val reason: String? = null)
