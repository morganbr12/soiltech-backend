package com.soiltech.backend.infrastructure.persistence.adapter

import com.soiltech.backend.domain.entity.CustomerProduceOrder
import com.soiltech.backend.domain.enum.ProduceOrderStatus
import com.soiltech.backend.domain.enum.ProducePaymentStatus
import com.soiltech.backend.domain.repository.CustomerProduceOrderRepository
import com.soiltech.backend.infrastructure.persistence.entity.CustomerProduceOrderJpaEntity
import com.soiltech.backend.infrastructure.persistence.jpa.CustomerProduceOrderJpaRepository
import jakarta.persistence.criteria.Predicate
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Component
class CustomerProduceOrderRepositoryAdapter(
    private val jpa: CustomerProduceOrderJpaRepository
) : CustomerProduceOrderRepository {

    override fun findById(id: UUID): CustomerProduceOrder? =
        jpa.findById(id).orElse(null)?.toDomain()

    override fun findAll(
        status: ProduceOrderStatus?,
        paymentStatus: ProducePaymentStatus?,
        region: String?,
        customerId: UUID?,
        search: String?,
        pageable: Pageable
    ): Page<CustomerProduceOrder> {
        val spec = Specification<CustomerProduceOrderJpaEntity> { root, _, cb ->
            val predicates = mutableListOf<Predicate>()
            status?.let { predicates.add(cb.equal(root.get<ProduceOrderStatus>("status"), it)) }
            paymentStatus?.let { predicates.add(cb.equal(root.get<ProducePaymentStatus>("paymentStatus"), it)) }
            region?.let { predicates.add(cb.equal(cb.lower(root.get("region")), it.lowercase())) }
            customerId?.let { predicates.add(cb.equal(root.get<UUID>("customerId"), it)) }
            search?.let { q ->
                val like = "%${q.lowercase()}%"
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("orderCode")), like),
                    cb.like(cb.lower(root.get("customerName")), like),
                    cb.like(cb.lower(root.get("produce")), like)
                ))
            }
            cb.and(*predicates.toTypedArray())
        }
        return jpa.findAll(spec, pageable).map { it.toDomain() }
    }

    override fun save(order: CustomerProduceOrder): CustomerProduceOrder {
        val existing = jpa.findById(order.id).orElse(null)
        return if (existing != null) {
            existing.apply {
                status = order.status
                paymentStatus = order.paymentStatus
                assignedAgent = order.assignedAgent
                assignedDriver = order.assignedDriver
                cancellationReason = order.cancellationReason
                deliveryDate = order.deliveryDate
            }
            jpa.save(existing).toDomain()
        } else {
            jpa.save(CustomerProduceOrderJpaEntity.fromDomain(order)).toDomain()
        }
    }

    override fun countByStatus(): Map<ProduceOrderStatus, Long> {
        return jpa.countGroupByStatus().associate { row ->
            val status = when (val v = row[0]) {
                is ProduceOrderStatus -> v
                is String -> ProduceOrderStatus.entries.firstOrNull { it.name == v } ?: return@associate null to 0L
                else -> return@associate null to 0L
            }
            status to (row[1] as Number).toLong()
        }.filterKeys { it != null } as Map<ProduceOrderStatus, Long>
    }

    override fun countByPaymentStatus(): Map<ProducePaymentStatus, Long> {
        return jpa.countGroupByPaymentStatus().associate { row ->
            val ps = when (val v = row[0]) {
                is ProducePaymentStatus -> v
                is String -> ProducePaymentStatus.entries.firstOrNull { it.name == v } ?: return@associate null to 0L
                else -> return@associate null to 0L
            }
            ps to (row[1] as Number).toLong()
        }.filterKeys { it != null } as Map<ProducePaymentStatus, Long>
    }

    override fun sumTotalValue(): BigDecimal = jpa.sumTotalValue()
    override fun countAll(): Long = jpa.countAll()
    override fun existsByOrderCode(code: String): Boolean = jpa.existsByOrderCode(code)
    override fun findLastOrderDateByCustomerId(customerId: UUID): LocalDateTime? =
        jpa.findLastOrderDateByCustomerId(customerId)
    override fun sumTotalSpentByCustomerId(customerId: UUID): BigDecimal =
        jpa.sumTotalSpentByCustomerId(customerId)
    override fun countByCustomerId(customerId: UUID): Long = jpa.countByCustomerId(customerId)

    override fun findTotalSpentByCustomerIds(ids: List<UUID>): Map<UUID, BigDecimal> =
        jpa.sumTotalSpentByCustomerIds(ids).associate { row -> (row[0] as UUID) to (row[1] as BigDecimal) }

    override fun findOrderCountByCustomerIds(ids: List<UUID>): Map<UUID, Long> =
        jpa.countByCustomerIds(ids).associate { row -> (row[0] as UUID) to (row[1] as Number).toLong() }

    override fun findLastOrderDateByCustomerIds(ids: List<UUID>): Map<UUID, LocalDateTime> =
        jpa.findLastOrderDatesByCustomerIds(ids).associate { row -> (row[0] as UUID) to (row[1] as LocalDateTime) }

    override fun sumMonthlyRevenue(year: Int): List<BigDecimal> {
        val raw = jpa.sumMonthlyRevenueByYear(year)
        val monthMap = raw.associate { row -> (row[0] as Number).toInt() to (row[1] as BigDecimal) }
        return (1..12).map { m -> monthMap[m] ?: BigDecimal.ZERO }
    }

    override fun findRecentOrders(limit: Int): List<CustomerProduceOrder> =
        jpa.findRecentOrders(PageRequest.of(0, limit)).map { it.toDomain() }
}
