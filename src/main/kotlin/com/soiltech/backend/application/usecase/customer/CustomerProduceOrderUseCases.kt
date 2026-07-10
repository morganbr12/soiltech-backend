package com.soiltech.backend.application.usecase.customer

import com.soiltech.backend.application.dto.customer.*
import com.soiltech.backend.domain.entity.CustomerProduceOrder
import com.soiltech.backend.domain.enum.ProduceOrderStatus
import com.soiltech.backend.domain.enum.ProducePaymentStatus
import com.soiltech.backend.domain.repository.AgentRepository
import com.soiltech.backend.domain.repository.CustomerProduceOrderRepository
import com.soiltech.backend.domain.repository.CustomerProfileRepository
import com.soiltech.backend.domain.repository.FarmerRepository
import com.soiltech.backend.interfaces.exception.BadRequestException
import com.soiltech.backend.interfaces.exception.NotFoundException
import com.soiltech.backend.interfaces.response.PaginationMeta
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Service
class ListProduceOrdersUseCase(
    private val orderRepository: CustomerProduceOrderRepository,
    private val customerProfileRepository: CustomerProfileRepository,
    private val farmerRepository: FarmerRepository,
    private val agentRepository: AgentRepository
) {
    fun execute(
        status: ProduceOrderStatus?,
        paymentStatus: ProducePaymentStatus?,
        region: String?,
        customerId: UUID?,
        search: String?,
        page: Int,
        limit: Int,
        sortBy: String,
        sortOrder: String
    ): Triple<List<ProduceOrderResponse>, ProduceOrderSummaryResponse, PaginationMeta> {
        val direction = if (sortOrder.equals("asc", ignoreCase = true)) Sort.Direction.ASC else Sort.Direction.DESC
        val sortField = when (sortBy) { "totalAmount" -> "totalAmount"; "quantityKg" -> "quantityKg"; else -> "createdAt" }
        val pageable = PageRequest.of((page - 1).coerceAtLeast(0), limit.coerceIn(1, 100), Sort.by(direction, sortField))

        val resultPage = orderRepository.findAll(status, paymentStatus, region, customerId, search, pageable)
        val statusCounts = orderRepository.countByStatus()
        val paymentCounts = orderRepository.countByPaymentStatus()
        val totalValue = orderRepository.sumTotalValue()

        val summary = ProduceOrderSummaryResponse(
            total = orderRepository.countAll(),
            pending = statusCounts.getOrDefault(ProduceOrderStatus.PENDING, 0L),
            confirmed = statusCounts.getOrDefault(ProduceOrderStatus.CONFIRMED, 0L),
            processing = statusCounts.getOrDefault(ProduceOrderStatus.PROCESSING, 0L),
            delivered = statusCounts.getOrDefault(ProduceOrderStatus.DELIVERED, 0L),
            cancelled = statusCounts.getOrDefault(ProduceOrderStatus.CANCELLED, 0L),
            unpaid = paymentCounts.getOrDefault(ProducePaymentStatus.UNPAID, 0L),
            totalValue = totalValue
        )
        val orders = resultPage.content
        val customerIds = orders.map { it.customerId }.distinct()
        val farmerIds = orders.mapNotNull { it.farmerId }.distinct()
        val agentIds = orders.mapNotNull { it.agentId }.distinct()
        val customerMap = customerProfileRepository.findByIds(customerIds)
        val farmerMap = farmerRepository.findByIds(farmerIds).associateBy { it.id }
        val agentMap = agentRepository.findByIds(agentIds)
        return Triple(
            orders.map { it.toResponse(customerMap[it.customerId], farmerMap[it.farmerId], agentMap[it.agentId]) },
            summary,
            PaginationMeta.from(resultPage, page, limit)
        )
    }
}

@Service
class CreateProduceOrderUseCase(
    private val orderRepository: CustomerProduceOrderRepository,
    private val customerProfileRepository: CustomerProfileRepository,
    private val farmerRepository: FarmerRepository,
    private val agentRepository: AgentRepository
) {
    @Transactional
    fun execute(request: CreateProduceOrderRequest, userId: UUID? = null): ProduceOrderResponse {
        val customer = when {
            request.customerId != null -> customerProfileRepository.findById(request.customerId)
                ?: throw NotFoundException("Customer not found with id: ${request.customerId}")
            userId != null -> customerProfileRepository.findByUserId(userId)
                ?: throw NotFoundException("Customer profile not found")
            else -> throw com.soiltech.backend.interfaces.exception.BadRequestException("customer_id is required")
        }

        val farmer = request.farmerId?.let { farmerRepository.findById(it) }
        val agent = request.agentId?.let { agentRepository.findById(it) }

        val now = LocalDateTime.now()
        val orderCode = generateUniqueCode(orderRepository)
        val totalAmount = request.pricePerKg.multiply(java.math.BigDecimal(request.quantityKg))

        val order = orderRepository.save(
            CustomerProduceOrder(
                id = UUID.randomUUID(),
                orderCode = orderCode,
                customerId = customer.id,
                customerCode = customer.customerCode ?: "",
                customerName = customer.fullName,
                farmerId = farmer?.id,
                agentId = agent?.id,
                produce = request.produce,
                quantityKg = request.quantityKg,
                pricePerKg = request.pricePerKg,
                totalAmount = totalAmount,
                status = ProduceOrderStatus.PENDING,
                paymentStatus = ProducePaymentStatus.UNPAID,
                assignedAgent = agent?.let { "${it.firstName} ${it.lastName}" } ?: request.assignedAgent,
                assignedDriver = null,
                region = request.region ?: "",
                cancellationReason = null,
                orderDate = LocalDate.now(),
                deliveryDate = null,
                createdAt = now,
                updatedAt = now,
                farmerName = farmer?.let { "${it.firstName} ${it.lastName}" },
                farmerPhone = farmer?.phone,
                agentPhone = agent?.phone
            )
        )
        return order.toResponse()
    }

    private fun generateUniqueCode(repo: CustomerProduceOrderRepository): String {
        val count = repo.countAll()
        var num = (count + 1).toInt()
        var code: String
        do { code = "ORD-${String.format("%05d", num++)}" } while (repo.existsByOrderCode(code))
        return code
    }
}

@Service
class ConfirmOrderUseCase(private val orderRepository: CustomerProduceOrderRepository) {
    @Transactional
    fun execute(id: UUID): ProduceOrderResponse {
        val order = orderRepository.findById(id) ?: throw NotFoundException("Order not found with id: $id")
        if (order.status != ProduceOrderStatus.PENDING)
            throw BadRequestException("Only PENDING orders can be confirmed. Current status: ${order.status.value}")
        val updated = order.copy(status = ProduceOrderStatus.CONFIRMED, updatedAt = LocalDateTime.now())
        return orderRepository.save(updated).toResponse()
    }
}

@Service
class CancelOrderUseCase(private val orderRepository: CustomerProduceOrderRepository) {
    @Transactional
    fun execute(id: UUID, request: CancelOrderRequest): ProduceOrderResponse {
        val order = orderRepository.findById(id) ?: throw NotFoundException("Order not found with id: $id")
        if (order.status in listOf(ProduceOrderStatus.DELIVERED, ProduceOrderStatus.CANCELLED))
            throw BadRequestException("Cannot cancel an order with status: ${order.status.value}")
        val updated = order.copy(
            status = ProduceOrderStatus.CANCELLED,
            cancellationReason = request.reason,
            updatedAt = LocalDateTime.now()
        )
        return orderRepository.save(updated).toResponse()
    }
}

@Service
class DeliverOrderUseCase(private val orderRepository: CustomerProduceOrderRepository) {
    @Transactional
    fun execute(id: UUID): ProduceOrderResponse {
        val order = orderRepository.findById(id) ?: throw NotFoundException("Order not found with id: $id")
        if (order.status !in listOf(ProduceOrderStatus.CONFIRMED, ProduceOrderStatus.PROCESSING))
            throw BadRequestException("Only CONFIRMED or PROCESSING orders can be marked as delivered. Current status: ${order.status.value}")
        val updated = order.copy(
            status = ProduceOrderStatus.DELIVERED,
            deliveryDate = LocalDate.now(),
            updatedAt = LocalDateTime.now()
        )
        return orderRepository.save(updated).toResponse()
    }
}

// ── Mapper ────────────────────────────────────────────────────────────────────

private fun CustomerProduceOrder.toResponse(
    customer: com.soiltech.backend.domain.entity.CustomerProfile? = null,
    farmer: com.soiltech.backend.domain.entity.Farmer? = null,
    agent: com.soiltech.backend.domain.entity.Agent? = null
) = ProduceOrderResponse(
    id = id, orderCode = orderCode, customerId = customerId, customerCode = customerCode,
    customerName = customerName,
    customer = customer?.let {
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
    produce = produce, quantityKg = quantityKg,
    pricePerKg = pricePerKg, totalAmount = totalAmount, status = status,
    paymentStatus = paymentStatus, assignedAgent = assignedAgent, assignedDriver = assignedDriver,
    orderDate = orderDate, deliveryDate = deliveryDate, region = region,
    cancellationReason = cancellationReason,
    farmerName = farmerName, farmerPhone = farmerPhone, agentPhone = agentPhone,
    createdAt = createdAt, updatedAt = updatedAt
)
