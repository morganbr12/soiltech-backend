package com.soiltech.backend.application.usecase.order

import com.soiltech.backend.application.dto.order.*
import com.soiltech.backend.application.mapper.toDto
import com.soiltech.backend.application.mapper.toListDto
import com.soiltech.backend.domain.entity.CustomerOrder
import com.soiltech.backend.domain.entity.OrderItem
import com.soiltech.backend.domain.entity.OrderTimeline
import com.soiltech.backend.domain.enum.NotificationType
import com.soiltech.backend.domain.enum.OrderStatus
import com.soiltech.backend.domain.enum.ProduceOrderStatus
import com.soiltech.backend.domain.repository.CustomerOrderRepository
import com.soiltech.backend.domain.repository.CustomerProduceOrderRepository
import com.soiltech.backend.domain.repository.CustomerProfileRepository
import com.soiltech.backend.domain.repository.ProduceListingRepository
import com.soiltech.backend.domain.repository.ProductRepository
import com.soiltech.backend.infrastructure.service.NotificationService
import com.soiltech.backend.interfaces.exception.BadRequestException
import com.soiltech.backend.interfaces.exception.ForbiddenException
import com.soiltech.backend.interfaces.exception.NotFoundException
import com.soiltech.backend.interfaces.response.PaginationMeta
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Service
class PlaceOrderUseCase(
    private val customerOrderRepository: CustomerOrderRepository,
    private val customerProfileRepository: CustomerProfileRepository,
    private val productRepository: ProductRepository,
    private val produceListingRepository: ProduceListingRepository,
    private val eventPublisher: ApplicationEventPublisher,
    private val notificationService: NotificationService
) {
    @Transactional
    fun execute(request: PlaceOrderRequest, userId: UUID): CustomerOrderDto {
        val customer = customerProfileRepository.findByUserId(userId)
            ?: throw NotFoundException("Customer profile not found")

        val now = LocalDateTime.now()
        val orderId = UUID.randomUUID()

        val items = request.items.map { itemReq ->
            val product = productRepository.findById(itemReq.productId)
                ?: throw NotFoundException("Product ${itemReq.productId} not found")
            if (!product.isAvailable) throw BadRequestException("Product '${product.name}' is not available")
            if (product.stockQuantity < itemReq.quantity) {
                throw BadRequestException("Insufficient stock for product '${product.name}'")
            }
            val listing = product.produceListingId?.let { produceListingRepository.findById(it) }
            OrderItem(
                id = UUID.randomUUID(),
                orderId = orderId,
                productId = product.id,
                productName = product.name,
                agentName = listing?.agentName,
                region = product.location,
                quantity = itemReq.quantity,
                unitPrice = product.pricePerUnit,
                subtotal = product.pricePerUnit.multiply(BigDecimal(itemReq.quantity))
            )
        }

        val totalAmount = items.fold(BigDecimal.ZERO) { acc, item -> acc.add(item.subtotal) }

        val order = customerOrderRepository.saveOrder(
            CustomerOrder(
                id = orderId,
                customerId = customer.id,
                customerName = customer.fullName,
                status = OrderStatus.PENDING,
                totalAmount = totalAmount,
                deliveryAddress = request.deliveryAddress,
                paymentType = request.paymentType,
                notes = request.notes,
                createdAt = now,
                updatedAt = now
            )
        )

        val savedItems = customerOrderRepository.saveItems(items)

        val timeline = customerOrderRepository.saveTimeline(
            OrderTimeline(
                id = UUID.randomUUID(),
                orderId = orderId,
                status = OrderStatus.PENDING,
                note = "Order placed",
                createdAt = now,
                createdBy = userId
            )
        )

        eventPublisher.publishEvent(
            com.soiltech.backend.domain.event.OrderPlacedEvent(
                orderId = orderId,
                customerId = customer.id,
                totalAmount = totalAmount
            )
        )

        notificationService.pushToAdmins(
            title = "New Order Received",
            body = "${customer.fullName} placed an order worth GHS ${totalAmount}.",
            type = NotificationType.ORDER_PLACED,
            referenceId = orderId,
            referenceType = "ORDER"
        )

        return order.toDto(savedItems, listOf(timeline))
    }
}

@Service
class ListOrdersUseCase(
    private val customerProduceOrderRepository: CustomerProduceOrderRepository,
    private val customerProfileRepository: CustomerProfileRepository
) {
    fun execute(userId: UUID, statuses: List<OrderStatus>?, page: Int, perPage: Int): Pair<List<CustomerOrderListDto>, PaginationMeta> {
        val customer = customerProfileRepository.findByUserId(userId)
            ?: throw NotFoundException("Customer profile not found")
        val pageable = PageRequest.of(page - 1, perPage, Sort.by("createdAt").descending())
        val produceStatus = statuses?.firstOrNull()?.toProduceStatus()
        val result = customerProduceOrderRepository.findAll(produceStatus, null, null, customer.id, null, pageable)
        val dtos = result.content.map { order ->
            CustomerOrderListDto(
                id = order.id,
                customerId = order.customerId,
                customerName = order.customerName,
                status = order.status.toOrderStatus(),
                totalAmount = order.totalAmount,
                deliveryAddress = order.region,
                paymentType = null,
                itemCount = 1,
                createdAt = order.createdAt,
                updatedAt = order.updatedAt
            )
        }
        return dtos to PaginationMeta.from(result, page, perPage)
    }

    private fun OrderStatus.toProduceStatus(): ProduceOrderStatus? = when (this) {
        OrderStatus.PENDING -> ProduceOrderStatus.PENDING
        OrderStatus.CONFIRMED -> ProduceOrderStatus.CONFIRMED
        OrderStatus.PROCESSING -> ProduceOrderStatus.PROCESSING
        OrderStatus.DELIVERED -> ProduceOrderStatus.DELIVERED
        OrderStatus.CANCELLED -> ProduceOrderStatus.CANCELLED
        else -> null
    }

    private fun ProduceOrderStatus.toOrderStatus(): OrderStatus = when (this) {
        ProduceOrderStatus.PENDING -> OrderStatus.PENDING
        ProduceOrderStatus.CONFIRMED -> OrderStatus.CONFIRMED
        ProduceOrderStatus.PROCESSING -> OrderStatus.PROCESSING
        ProduceOrderStatus.DELIVERED -> OrderStatus.DELIVERED
        ProduceOrderStatus.CANCELLED -> OrderStatus.CANCELLED
    }
}

@Service
class GetOrderUseCase(
    private val customerOrderRepository: CustomerOrderRepository,
    private val customerProfileRepository: CustomerProfileRepository
) {
    fun execute(orderId: UUID, userId: UUID): CustomerOrderDto {
        val customer = customerProfileRepository.findByUserId(userId)
            ?: throw NotFoundException("Customer profile not found")
        val order = customerOrderRepository.findById(orderId)
            ?: throw NotFoundException("Order not found")
        if (order.customerId != customer.id) throw ForbiddenException("Access denied")

        val items = customerOrderRepository.findItemsByOrderId(orderId)
        val timeline = customerOrderRepository.findTimelineByOrderId(orderId)
        return order.toDto(items, timeline)
    }
}

@Service
class ListAllOrdersAdminUseCase(
    private val customerOrderRepository: CustomerOrderRepository
) {
    fun execute(
        customerId: UUID?,
        statuses: List<OrderStatus>?,
        page: Int,
        perPage: Int,
        sortOrder: String
    ): Pair<List<CustomerOrderListDto>, PaginationMeta> {
        val direction = if (sortOrder.equals("asc", ignoreCase = true)) Sort.Direction.ASC else Sort.Direction.DESC
        val pageable = PageRequest.of((page - 1).coerceAtLeast(0), perPage, Sort.by(direction, "createdAt"))
        val result = customerOrderRepository.findAllAdmin(customerId, statuses, pageable)
        val dtos = result.content.map { order ->
            val itemCount = customerOrderRepository.findItemsByOrderId(order.id).size
            order.toListDto(itemCount)
        }
        return dtos to PaginationMeta.from(result, page, perPage)
    }
}

@Service
class GetCustomerOrdersUseCase(
    private val customerOrderRepository: CustomerOrderRepository,
    private val customerProfileRepository: CustomerProfileRepository
) {
    fun execute(customerId: UUID, statuses: List<OrderStatus>?, page: Int, perPage: Int): Pair<List<CustomerOrderListDto>, PaginationMeta> {
        customerProfileRepository.findById(customerId)
            ?: throw NotFoundException("Customer not found")
        val pageable = PageRequest.of((page - 1).coerceAtLeast(0), perPage, Sort.by("createdAt").descending())
        val result = customerOrderRepository.findAllAdmin(customerId, statuses, pageable)
        val dtos = result.content.map { order ->
            val itemCount = customerOrderRepository.findItemsByOrderId(order.id).size
            order.toListDto(itemCount)
        }
        return dtos to PaginationMeta.from(result, page, perPage)
    }
}

@Service
class UpdateOrderStatusUseCase(
    private val customerOrderRepository: CustomerOrderRepository,
    private val notificationService: NotificationService
) {
    @Transactional
    fun execute(orderId: UUID, request: UpdateOrderStatusRequest, userId: UUID): CustomerOrderDto {
        val order = customerOrderRepository.findById(orderId)
            ?: throw NotFoundException("Order not found")

        val updatedOrder = customerOrderRepository.updateStatus(orderId, request.status)

        customerOrderRepository.saveTimeline(
            OrderTimeline(
                id = UUID.randomUUID(),
                orderId = orderId,
                status = request.status,
                note = request.note,
                createdAt = LocalDateTime.now(),
                createdBy = userId
            )
        )

        when (request.status) {
            OrderStatus.CONFIRMED -> notificationService.pushToCustomer(
                customerProfileId = order.customerId,
                title = "Order Confirmed",
                body = "Your order has been confirmed and is being prepared.",
                type = NotificationType.ORDER_CONFIRMED,
                referenceId = orderId,
                referenceType = "ORDER"
            )
            OrderStatus.AGENT_CONFIRMED -> notificationService.pushToCustomer(
                customerProfileId = order.customerId,
                title = "Order Ready for Pickup",
                body = "Your order has been verified in the field and is ready for pickup.",
                type = NotificationType.ORDER_AGENT_CONFIRMED,
                referenceId = orderId,
                referenceType = "ORDER"
            )
            OrderStatus.SHIPPED -> notificationService.pushToCustomer(
                customerProfileId = order.customerId,
                title = "Order On the Way",
                body = "Your order is on the way!",
                type = NotificationType.ORDER_SHIPPED,
                referenceId = orderId,
                referenceType = "ORDER"
            )
            OrderStatus.DELIVERED -> notificationService.pushToCustomer(
                customerProfileId = order.customerId,
                title = "Order Delivered",
                body = "Your order has been delivered. Thank you!",
                type = NotificationType.ORDER_DELIVERED,
                referenceId = orderId,
                referenceType = "ORDER"
            )
            OrderStatus.CANCELLED -> notificationService.pushToCustomer(
                customerProfileId = order.customerId,
                title = "Order Cancelled",
                body = "Your order has been cancelled.",
                type = NotificationType.ORDER_CANCELLED,
                referenceId = orderId,
                referenceType = "ORDER"
            )
            else -> Unit
        }

        val items = customerOrderRepository.findItemsByOrderId(orderId)
        val timeline = customerOrderRepository.findTimelineByOrderId(orderId)
        return updatedOrder.toDto(items, timeline)
    }
}
