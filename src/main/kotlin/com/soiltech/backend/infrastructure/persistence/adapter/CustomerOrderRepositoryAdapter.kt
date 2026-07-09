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
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
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
}
