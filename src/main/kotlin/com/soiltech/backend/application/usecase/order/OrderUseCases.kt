package com.soiltech.backend.application.usecase.order

import com.soiltech.backend.application.dto.order.*
import com.soiltech.backend.application.mapper.toDto
import com.soiltech.backend.application.mapper.toListDto
import com.soiltech.backend.domain.entity.CustomerOrder
import com.soiltech.backend.domain.entity.OrderItem
import com.soiltech.backend.domain.entity.OrderTimeline
import com.soiltech.backend.domain.enum.OrderStatus
import com.soiltech.backend.domain.repository.CustomerOrderRepository
import com.soiltech.backend.domain.repository.CustomerProfileRepository
import com.soiltech.backend.domain.repository.ProductRepository
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
    private val eventPublisher: ApplicationEventPublisher
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
            OrderItem(
                id = UUID.randomUUID(),
                orderId = orderId,
                productId = product.id,
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
                status = OrderStatus.PENDING,
                totalAmount = totalAmount,
                deliveryAddress = request.deliveryAddress,
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

        return order.toDto(savedItems, listOf(timeline))
    }
}

@Service
class ListOrdersUseCase(
    private val customerOrderRepository: CustomerOrderRepository,
    private val customerProfileRepository: CustomerProfileRepository
) {
    fun execute(userId: UUID, status: OrderStatus?, page: Int, perPage: Int): Pair<List<CustomerOrderListDto>, PaginationMeta> {
        val customer = customerProfileRepository.findByUserId(userId)
            ?: throw NotFoundException("Customer profile not found")
        val pageable = PageRequest.of(page - 1, perPage, Sort.by("createdAt").descending())
        val result = customerOrderRepository.findAll(customer.id, status, pageable)
        val dtos = result.content.map { order ->
            val items = customerOrderRepository.findItemsByOrderId(order.id)
            order.toListDto(items.size)
        }
        return dtos to PaginationMeta.from(result, page, perPage)
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
class UpdateOrderStatusUseCase(
    private val customerOrderRepository: CustomerOrderRepository
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

        val items = customerOrderRepository.findItemsByOrderId(orderId)
        val timeline = customerOrderRepository.findTimelineByOrderId(orderId)
        return updatedOrder.toDto(items, timeline)
    }
}
