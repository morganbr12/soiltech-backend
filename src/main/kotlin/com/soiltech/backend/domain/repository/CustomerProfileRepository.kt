package com.soiltech.backend.domain.repository

import com.soiltech.backend.domain.entity.CustomerMetrics
import com.soiltech.backend.domain.entity.CustomerProfile
import com.soiltech.backend.domain.enum.CustomerStatus
import com.soiltech.backend.domain.enum.CustomerTier
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface CustomerProfileRepository {
    fun findById(id: UUID): CustomerProfile?
    fun findByUserId(userId: UUID): CustomerProfile?
    fun save(profile: CustomerProfile): CustomerProfile
    fun update(profile: CustomerProfile): CustomerProfile
    fun delete(id: UUID)

    // Admin list + filter
    fun findAll(
        status: CustomerStatus?,
        tier: CustomerTier?,
        region: String?,
        search: String?,
        pageable: Pageable
    ): Page<CustomerProfile>

    // Aggregates for summary block
    fun countByStatus(): Map<CustomerStatus, Long>
    fun countByTier(): Map<CustomerTier, Long>
    fun countAll(): Long

    // Batch metrics (totalOrders, totalSpent, walletBalance, lastOrderDate)
    fun findMetricsByCustomerIds(ids: List<UUID>): Map<UUID, CustomerMetrics>

    // Dashboard queries
    fun findTopCustomers(limit: Int): List<CustomerProfile>
    fun countMonthlyNewCustomers(year: Int): List<Long>

    fun avgRating(): Double

    // Uniqueness checks
    fun existsByCustomerCode(code: String): Boolean
    fun existsByNationalId(nationalId: String): Boolean
    fun existsByNationalIdAndIdNot(nationalId: String, id: UUID): Boolean
    fun existsByPhone(phone: String): Boolean
    fun existsByPhoneAndIdNot(phone: String, id: UUID): Boolean
    fun existsByEmail(email: String): Boolean
    fun existsByEmailAndIdNot(email: String, id: UUID): Boolean
}
