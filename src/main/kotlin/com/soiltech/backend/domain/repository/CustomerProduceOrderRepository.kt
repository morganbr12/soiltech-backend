package com.soiltech.backend.domain.repository

import com.soiltech.backend.domain.entity.CustomerProduceOrder
import com.soiltech.backend.domain.enum.ProduceOrderStatus
import com.soiltech.backend.domain.enum.ProducePaymentStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

interface CustomerProduceOrderRepository {
    fun findById(id: UUID): CustomerProduceOrder?
    fun findAll(
        status: ProduceOrderStatus?,
        paymentStatus: ProducePaymentStatus?,
        region: String?,
        customerId: UUID?,
        search: String?,
        pageable: Pageable
    ): Page<CustomerProduceOrder>
    fun save(order: CustomerProduceOrder): CustomerProduceOrder
    fun countByStatus(): Map<ProduceOrderStatus, Long>
    fun countByPaymentStatus(): Map<ProducePaymentStatus, Long>
    fun sumTotalValue(): BigDecimal
    fun countAll(): Long
    fun existsByOrderCode(code: String): Boolean
    fun findLastOrderDateByCustomerId(customerId: UUID): LocalDateTime?
    fun sumTotalSpentByCustomerId(customerId: UUID): BigDecimal
    fun countByCustomerId(customerId: UUID): Long
    fun findTotalSpentByCustomerIds(ids: List<UUID>): Map<UUID, BigDecimal>
    fun findOrderCountByCustomerIds(ids: List<UUID>): Map<UUID, Long>
    fun findLastOrderDateByCustomerIds(ids: List<UUID>): Map<UUID, LocalDateTime>
    fun sumMonthlyRevenue(year: Int): List<BigDecimal>
    fun findRecentOrders(limit: Int): List<CustomerProduceOrder>
}
