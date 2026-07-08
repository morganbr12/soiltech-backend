package com.soiltech.backend.infrastructure.persistence.jpa

import com.soiltech.backend.infrastructure.persistence.entity.CustomerProfileJpaEntity
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface CustomerProfileJpaRepository :
    JpaRepository<CustomerProfileJpaEntity, UUID>,
    JpaSpecificationExecutor<CustomerProfileJpaEntity> {

    fun findByUserId(userId: UUID): CustomerProfileJpaEntity?

    fun existsByCustomerCode(code: String): Boolean
    fun existsByNationalId(nationalId: String): Boolean
    fun existsByNationalIdAndIdNot(nationalId: String, id: UUID): Boolean
    fun existsByPhone(phone: String): Boolean
    fun existsByPhoneAndIdNot(phone: String, id: UUID): Boolean
    fun existsByEmail(email: String): Boolean
    fun existsByEmailAndIdNot(email: String, id: UUID): Boolean

    @Query("SELECT c.status, COUNT(c) FROM CustomerProfileJpaEntity c GROUP BY c.status")
    fun countGroupByStatus(): List<Array<Any>>

    @Query("SELECT c.tier, COUNT(c) FROM CustomerProfileJpaEntity c GROUP BY c.tier")
    fun countGroupByTier(): List<Array<Any>>

    @Query("SELECT COUNT(c) FROM CustomerProfileJpaEntity c")
    fun countAll(): Long

    @Query("SELECT c FROM CustomerProfileJpaEntity c ORDER BY c.createdAt DESC")
    fun findTopByOrderByCreatedAtDesc(pageable: Pageable): List<CustomerProfileJpaEntity>

    @Query("SELECT COALESCE(AVG(c.rating), 0.0) FROM CustomerProfileJpaEntity c")
    fun avgRating(): Double

    @Query("""
        SELECT MONTH(c.createdAt), COUNT(c)
        FROM CustomerProfileJpaEntity c
        WHERE YEAR(c.createdAt) = :year
        GROUP BY MONTH(c.createdAt)
        ORDER BY MONTH(c.createdAt)
    """)
    fun countMonthlyByYear(year: Int): List<Array<Any>>
}
