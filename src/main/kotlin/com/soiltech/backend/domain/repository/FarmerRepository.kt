package com.soiltech.backend.domain.repository

import com.soiltech.backend.domain.entity.Farmer
import com.soiltech.backend.domain.entity.FarmerMetrics
import com.soiltech.backend.domain.enum.FarmerStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class RegionalSummary(
    val region: String,
    val farmers: Long,
    val produce: Double,
    val revenue: BigDecimal
)

interface FarmerRepository {
    fun findById(id: UUID): Farmer?
    fun findAll(
        status: FarmerStatus?,
        region: String?,
        lbcId: UUID?,
        agentId: UUID?,
        kycVerified: Boolean?,
        search: String?,
        pageable: Pageable
    ): Page<Farmer>
    fun countByStatus(): Map<FarmerStatus, Long>
    fun findMetricsByFarmerIds(ids: List<UUID>): Map<UUID, FarmerMetrics>
    fun save(farmer: Farmer): Farmer
    fun delete(id: UUID)
    fun existsByPhone(phone: String): Boolean
    fun existsByPhoneAndIdNot(phone: String, id: UUID): Boolean
    fun existsByNationalId(nationalId: String): Boolean
    fun existsByNationalIdAndIdNot(nationalId: String, id: UUID): Boolean
    fun existsByFarmerCode(code: String): Boolean
    fun countAll(): Long

    // Dashboard aggregates
    fun countByAgentId(agentId: UUID): Long
    fun countApprovedByAgentId(agentId: UUID): Long
    fun findRecentByAgent(agentId: UUID, limit: Int): List<Farmer>
    fun findByIds(ids: List<UUID>): List<Farmer>

    // Admin dashboard
    fun findRegionalOverview(): List<RegionalSummary>
    fun findRecentGlobal(limit: Int): List<Farmer>
    fun countCreatedBetween(from: LocalDateTime, to: LocalDateTime): Long
}
