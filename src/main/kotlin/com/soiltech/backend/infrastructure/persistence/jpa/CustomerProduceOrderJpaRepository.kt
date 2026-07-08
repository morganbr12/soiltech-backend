package com.soiltech.backend.infrastructure.persistence.jpa

import com.soiltech.backend.infrastructure.persistence.entity.CustomerProduceOrderJpaEntity
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Repository
interface CustomerProduceOrderJpaRepository :
    JpaRepository<CustomerProduceOrderJpaEntity, UUID>,
    JpaSpecificationExecutor<CustomerProduceOrderJpaEntity> {

    fun existsByOrderCode(code: String): Boolean

    @Query("SELECT COUNT(o) FROM CustomerProduceOrderJpaEntity o")
    fun countAll(): Long

    @Query("SELECT o.status, COUNT(o) FROM CustomerProduceOrderJpaEntity o GROUP BY o.status")
    fun countGroupByStatus(): List<Array<Any>>

    @Query("SELECT o.paymentStatus, COUNT(o) FROM CustomerProduceOrderJpaEntity o GROUP BY o.paymentStatus")
    fun countGroupByPaymentStatus(): List<Array<Any>>

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM CustomerProduceOrderJpaEntity o")
    fun sumTotalValue(): BigDecimal

    @Query("SELECT MAX(o.createdAt) FROM CustomerProduceOrderJpaEntity o WHERE o.customerId = :customerId")
    fun findLastOrderDateByCustomerId(@Param("customerId") customerId: UUID): LocalDateTime?

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM CustomerProduceOrderJpaEntity o WHERE o.customerId = :customerId")
    fun sumTotalSpentByCustomerId(@Param("customerId") customerId: UUID): BigDecimal

    @Query("SELECT COUNT(o) FROM CustomerProduceOrderJpaEntity o WHERE o.customerId = :customerId")
    fun countByCustomerId(@Param("customerId") customerId: UUID): Long

    @Query("SELECT o.customerId, COALESCE(SUM(o.totalAmount), 0) FROM CustomerProduceOrderJpaEntity o WHERE o.customerId IN :ids GROUP BY o.customerId")
    fun sumTotalSpentByCustomerIds(@Param("ids") ids: List<UUID>): List<Array<Any>>

    @Query("SELECT o.customerId, COUNT(o) FROM CustomerProduceOrderJpaEntity o WHERE o.customerId IN :ids GROUP BY o.customerId")
    fun countByCustomerIds(@Param("ids") ids: List<UUID>): List<Array<Any>>

    @Query("SELECT o.customerId, MAX(o.createdAt) FROM CustomerProduceOrderJpaEntity o WHERE o.customerId IN :ids GROUP BY o.customerId")
    fun findLastOrderDatesByCustomerIds(@Param("ids") ids: List<UUID>): List<Array<Any>>

    @Query("""
        SELECT MONTH(o.orderDate), COALESCE(SUM(o.totalAmount), 0)
        FROM CustomerProduceOrderJpaEntity o
        WHERE YEAR(o.orderDate) = :year
        GROUP BY MONTH(o.orderDate)
        ORDER BY MONTH(o.orderDate)
    """)
    fun sumMonthlyRevenueByYear(@Param("year") year: Int): List<Array<Any>>

    @Query("SELECT o FROM CustomerProduceOrderJpaEntity o ORDER BY o.createdAt DESC")
    fun findRecentOrders(pageable: Pageable): List<CustomerProduceOrderJpaEntity>
}
