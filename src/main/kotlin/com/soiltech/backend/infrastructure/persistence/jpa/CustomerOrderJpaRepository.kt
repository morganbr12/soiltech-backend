package com.soiltech.backend.infrastructure.persistence.jpa

import com.soiltech.backend.domain.enum.OrderStatus
import com.soiltech.backend.infrastructure.persistence.entity.CustomerOrderJpaEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.util.UUID

interface TopSpenderProjection {
    fun getCustomerId(): UUID
    fun getOrderCount(): Long
    fun getTotalSpent(): BigDecimal
}

interface MonthlyRevenueProjection {
    fun getMonth(): Int
    fun getRevenue(): BigDecimal
}

@Repository
interface CustomerOrderJpaRepository : JpaRepository<CustomerOrderJpaEntity, UUID> {

    fun findByCustomerIdOrderByCreatedAtDesc(
        customerId: UUID,
        pageable: Pageable
    ): Page<CustomerOrderJpaEntity>

    fun findByCustomerIdAndStatusInOrderByCreatedAtDesc(
        customerId: UUID,
        statuses: List<OrderStatus>,
        pageable: Pageable
    ): Page<CustomerOrderJpaEntity>

    @Query("""
        SELECT o FROM CustomerOrderJpaEntity o
        WHERE (:customerId IS NULL OR o.customerId = :customerId)
          AND (:#{#statuses == null} = true OR o.status IN :statuses)
        ORDER BY o.createdAt DESC
    """)
    fun findAllFiltered(
        @Param("customerId") customerId: UUID?,
        @Param("statuses") statuses: List<OrderStatus>?,
        pageable: Pageable
    ): Page<CustomerOrderJpaEntity>

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM CustomerOrderJpaEntity o")
    fun sumTotalAmount(): BigDecimal

    @Query("SELECT o FROM CustomerOrderJpaEntity o ORDER BY o.createdAt DESC")
    fun findRecentOrders(pageable: Pageable): List<CustomerOrderJpaEntity>

    @Query(
        value = """
            SELECT EXTRACT(MONTH FROM created_at)::int AS month,
                   COALESCE(SUM(total_amount), 0) AS revenue
            FROM customer_orders
            WHERE EXTRACT(YEAR FROM created_at) = :year
            GROUP BY EXTRACT(MONTH FROM created_at)
            ORDER BY month
        """,
        nativeQuery = true
    )
    fun findMonthlyRevenue(@Param("year") year: Int): List<MonthlyRevenueProjection>

    @Query(
        value = """
            SELECT customer_id, COUNT(*) AS order_count, COALESCE(SUM(total_amount), 0) AS total_spent
            FROM customer_orders
            GROUP BY customer_id
            ORDER BY total_spent DESC
            LIMIT :lim
        """,
        nativeQuery = true
    )
    fun findTopSpenders(@Param("lim") limit: Int): List<TopSpenderProjection>

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM CustomerOrderJpaEntity o WHERE o.createdAt >= :from AND o.createdAt < :to")
    fun sumAmountBetween(
        @Param("from") from: java.time.LocalDateTime,
        @Param("to") to: java.time.LocalDateTime
    ): BigDecimal
}
