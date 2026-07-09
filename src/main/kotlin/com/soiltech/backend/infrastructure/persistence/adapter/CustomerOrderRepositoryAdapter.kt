package com.soiltech.backend.infrastructure.persistence.adapter

import com.soiltech.backend.domain.entity.CustomerOrder
import com.soiltech.backend.domain.entity.OrderItem
import com.soiltech.backend.domain.entity.OrderTimeline
import com.soiltech.backend.domain.enum.OrderStatus
import com.soiltech.backend.domain.repository.CustomerOrderRepository
import com.soiltech.backend.infrastructure.persistence.entity.CustomerOrderJpaEntity
import com.soiltech.backend.infrastructure.persistence.entity.OrderItemJpaEntity
import com.soiltech.backend.infrastructure.persistence.entity.OrderTimelineJpaEntity
import com.soiltech.backend.infrastructure.persistence.jpa.CustomerOrderJpaRepository
import com.soiltech.backend.infrastructure.persistence.jpa.OrderItemJpaRepository
import com.soiltech.backend.infrastructure.persistence.jpa.OrderTimelineJpaRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.util.UUID

@Component
class CustomerOrderRepositoryAdapter(
    private val orderJpaRepository: CustomerOrderJpaRepository,
    private val itemJpaRepository: OrderItemJpaRepository,
    private val timelineJpaRepository: OrderTimelineJpaRepository
) : CustomerOrderRepository {

    override fun findById(id: UUID): CustomerOrder? =
        orderJpaRepository.findById(id).orElse(null)?.toDomain()

    override fun findAll(customerId: UUID, statuses: List<OrderStatus>?, pageable: Pageable): Page<CustomerOrder> =
        if (statuses == null)
            orderJpaRepository.findByCustomerIdOrderByCreatedAtDesc(customerId, pageable).map { it.toDomain() }
        else
            orderJpaRepository.findByCustomerIdAndStatusInOrderByCreatedAtDesc(customerId, statuses, pageable).map { it.toDomain() }

    override fun findAllAdmin(customerId: UUID?, statuses: List<OrderStatus>?, pageable: Pageable): Page<CustomerOrder> =
        orderJpaRepository.findAllFiltered(customerId, statuses, pageable).map { it.toDomain() }

    override fun saveOrder(order: CustomerOrder): CustomerOrder =
        orderJpaRepository.save(CustomerOrderJpaEntity.fromDomain(order)).toDomain()

    override fun saveItems(items: List<OrderItem>): List<OrderItem> =
        itemJpaRepository.saveAll(items.map { OrderItemJpaEntity.fromDomain(it) }).map { it.toDomain() }

    override fun saveTimeline(timeline: OrderTimeline): OrderTimeline =
        timelineJpaRepository.save(OrderTimelineJpaEntity.fromDomain(timeline)).toDomain()

    override fun findItemsByOrderId(orderId: UUID): List<OrderItem> =
        itemJpaRepository.findByOrderId(orderId).map { it.toDomain() }

    override fun findTimelineByOrderId(orderId: UUID): List<OrderTimeline> =
        timelineJpaRepository.findByOrderIdOrderByCreatedAtAsc(orderId).map { it.toDomain() }

    override fun updateStatus(orderId: UUID, status: OrderStatus): CustomerOrder {
        val entity = orderJpaRepository.findById(orderId).orElseThrow()
        entity.status = status
        return orderJpaRepository.save(entity).toDomain()
    }

    override fun countAll(): Long = orderJpaRepository.count()

    override fun sumTotalAmount(): BigDecimal = orderJpaRepository.sumTotalAmount()

    override fun findRecent(limit: Int): List<CustomerOrder> =
        orderJpaRepository.findRecentOrders(PageRequest.of(0, limit)).map { it.toDomain() }

    override fun sumMonthlyRevenue(year: Int): List<BigDecimal> {
        val rows = orderJpaRepository.findMonthlyRevenue(year).associate { it.getMonth() to it.getRevenue() }
        return (1..12).map { month -> rows[month] ?: BigDecimal.ZERO }
    }

    override fun findTopSpenders(limit: Int): List<Triple<UUID, Long, BigDecimal>> =
        orderJpaRepository.findTopSpenders(limit).map {
            Triple(it.getCustomerId(), it.getOrderCount(), it.getTotalSpent())
        }

    override fun sumAmountBetween(from: java.time.LocalDateTime, to: java.time.LocalDateTime): BigDecimal =
        orderJpaRepository.sumAmountBetween(from, to)
}
