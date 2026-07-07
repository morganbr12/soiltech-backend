package com.soiltech.backend.domain.repository

import com.soiltech.backend.domain.entity.CustomerOrder
import com.soiltech.backend.domain.entity.OrderItem
import com.soiltech.backend.domain.entity.OrderTimeline
import com.soiltech.backend.domain.enum.OrderStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface CustomerOrderRepository {
    fun findById(id: UUID): CustomerOrder?
    fun findAll(customerId: UUID, status: OrderStatus?, pageable: Pageable): Page<CustomerOrder>
    fun saveOrder(order: CustomerOrder): CustomerOrder
    fun saveItems(items: List<OrderItem>): List<OrderItem>
    fun saveTimeline(timeline: OrderTimeline): OrderTimeline
    fun findItemsByOrderId(orderId: UUID): List<OrderItem>
    fun findTimelineByOrderId(orderId: UUID): List<OrderTimeline>
    fun updateStatus(orderId: UUID, status: OrderStatus): CustomerOrder
}
