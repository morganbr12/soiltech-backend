package com.soiltech.backend.infrastructure.persistence.adapter

import com.soiltech.backend.domain.entity.Farmer
import com.soiltech.backend.domain.entity.FarmerMetrics
import com.soiltech.backend.domain.enum.FarmerStatus
import com.soiltech.backend.domain.repository.FarmerRepository
import com.soiltech.backend.domain.repository.RegionalSummary
import com.soiltech.backend.infrastructure.persistence.entity.FarmerJpaEntity
import com.soiltech.backend.infrastructure.persistence.jpa.AgentJpaRepository
import com.soiltech.backend.infrastructure.persistence.jpa.FarmerJpaRepository
import com.soiltech.backend.infrastructure.persistence.jpa.LbcJpaRepository
import jakarta.persistence.criteria.JoinType
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
class FarmerRepositoryAdapter(
    private val jpaRepository: FarmerJpaRepository,
    private val agentJpaRepository: AgentJpaRepository,
    private val lbcJpaRepository: LbcJpaRepository
) : FarmerRepository {

    override fun findById(id: UUID): Farmer? {
        val entity = jpaRepository.findById(id).orElse(null) ?: return null
        val agentName = resolveAgentName(entity.agentId)
        val lbcName = resolveLbcName(entity.lbcId)
        return entity.toDomain(agentName, lbcName)
    }

    override fun findAll(
        status: FarmerStatus?,
        region: String?,
        lbcId: UUID?,
        agentId: UUID?,
        kycVerified: Boolean?,
        search: String?,
        pageable: Pageable
    ): Page<Farmer> {
        val page = jpaRepository.findAll(buildSpec(status, region, lbcId, agentId, kycVerified, search), pageable)

        val agentIds = page.content.map { it.agentId }.distinct()
        val lbcIds = page.content.map { it.lbcId }.distinct()
        val agentMap = agentJpaRepository.findAllById(agentIds)
            .associateBy { it.id!! }
        val lbcMap = lbcJpaRepository.findAllById(lbcIds)
            .associateBy { it.id!! }

        return page.map { entity ->
            val agent = agentMap[entity.agentId]
            val agentName = if (agent != null) "${agent.firstName} ${agent.lastName}" else ""
            val lbcName = lbcMap[entity.lbcId]?.name ?: ""
            entity.toDomain(agentName, lbcName)
        }
    }

    override fun countByStatus(): Map<FarmerStatus, Long> {
        val result = mutableMapOf<FarmerStatus, Long>()
        FarmerStatus.entries.forEach { result[it] = 0L }
        jpaRepository.countGroupByStatus().forEach { row ->
            val status = when (val raw = row[0]) {
                is FarmerStatus -> raw
                is String -> FarmerStatus.entries.firstOrNull { it.name == raw || it.value == raw }
                else -> null
            }
            val count = (row[1] as? Long) ?: (row[1] as? Number)?.toLong() ?: 0L
            if (status != null) result[status] = count
        }
        return result
    }

    override fun findMetricsByFarmerIds(ids: List<UUID>): Map<UUID, FarmerMetrics> {
        if (ids.isEmpty()) return emptyMap()

        val metricsMap = ids.associateWith { FarmerMetrics() }.toMutableMap()

        jpaRepository.findFarmStatsByFarmerIds(ids).forEach { row ->
            val farmerId = toUUID(row[0]) ?: return@forEach
            val count = (row[1] as? Number)?.toLong() ?: 0L
            val size = (row[2] as? Number)?.toDouble() ?: 0.0
            metricsMap[farmerId] = (metricsMap[farmerId] ?: FarmerMetrics()).copy(
                farmsCount = count,
                totalFarmSize = size
            )
        }

        val cropTypesMap = mutableMapOf<UUID, MutableList<String>>()
        jpaRepository.findCropTypesByFarmerIds(ids).forEach { row ->
            val farmerId = toUUID(row[0]) ?: return@forEach
            val crop = row[1] as? String ?: return@forEach
            cropTypesMap.getOrPut(farmerId) { mutableListOf() }.add(crop)
        }
        cropTypesMap.forEach { (farmerId, crops) ->
            metricsMap[farmerId] = (metricsMap[farmerId] ?: FarmerMetrics()).copy(
                cropTypes = crops.distinct()
            )
        }

        jpaRepository.sumEarningsByFarmerIds(ids).forEach { row ->
            val farmerId = toUUID(row[0]) ?: return@forEach
            val earnings = when (val raw = row[1]) {
                is BigDecimal -> raw
                is Number -> BigDecimal(raw.toString())
                else -> BigDecimal.ZERO
            }
            metricsMap[farmerId] = (metricsMap[farmerId] ?: FarmerMetrics()).copy(
                totalEarnings = earnings
            )
        }

        return metricsMap
    }

    override fun save(farmer: Farmer): Farmer {
        val existing = jpaRepository.findById(farmer.id).orElse(null)
        val saved = if (existing != null) {
            existing.apply {
                firstName = farmer.firstName
                lastName = farmer.lastName
                fullName = "${farmer.firstName} ${farmer.lastName}"
                phone = farmer.phone
                email = farmer.email
                nationalId = farmer.nationalId
                agentId = farmer.agentId
                lbcId = farmer.lbcId
                region = farmer.region
                district = farmer.district
                status = farmer.status
                kycVerified = farmer.kycVerified
                latitude = farmer.latitude
                longitude = farmer.longitude
                cropTypesRaw = farmer.cropTypes.joinToString(",").ifEmpty { null }
                rejectionReason = farmer.rejectionReason
                joinedDate = farmer.joinedDate
            }
            jpaRepository.save(existing)
        } else {
            jpaRepository.save(
                FarmerJpaEntity(
                    id = farmer.id,
                    farmerCode = farmer.farmerCode,
                    firstName = farmer.firstName,
                    lastName = farmer.lastName,
                    fullName = "${farmer.firstName} ${farmer.lastName}",
                    phone = farmer.phone,
                    email = farmer.email,
                    nationalId = farmer.nationalId,
                    agentId = farmer.agentId,
                    lbcId = farmer.lbcId,
                    region = farmer.region,
                    district = farmer.district,
                    status = farmer.status,
                    kycVerified = farmer.kycVerified,
                    latitude = farmer.latitude,
                    longitude = farmer.longitude,
                    cropTypesRaw = farmer.cropTypes.joinToString(",").ifEmpty { null },
                    rejectionReason = farmer.rejectionReason,
                    joinedDate = farmer.joinedDate
                )
            )
        }
        return saved.toDomain(farmer.agentName, farmer.lbcName)
    }

    override fun delete(id: UUID) = jpaRepository.deleteById(id)

    override fun existsByPhone(phone: String): Boolean = jpaRepository.existsByPhone(phone)

    override fun existsByPhoneAndIdNot(phone: String, id: UUID): Boolean =
        jpaRepository.existsByPhoneAndIdNot(phone, id)

    override fun existsByNationalId(nationalId: String): Boolean =
        jpaRepository.existsByNationalId(nationalId)

    override fun existsByNationalIdAndIdNot(nationalId: String, id: UUID): Boolean =
        jpaRepository.existsByNationalIdAndIdNot(nationalId, id)

    override fun existsByFarmerCode(code: String): Boolean = jpaRepository.existsByFarmerCode(code)

    override fun countAll(): Long = jpaRepository.countAll()

    override fun countByAgentId(agentId: UUID): Long = jpaRepository.countByAgentId(agentId)

    override fun countApprovedByAgentId(agentId: UUID): Long =
        jpaRepository.countByAgentIdAndStatus(agentId, FarmerStatus.APPROVED)

    override fun findRecentByAgent(agentId: UUID, limit: Int): List<Farmer> {
        val entities = jpaRepository.findRecentByAgentId(agentId, PageRequest.of(0, limit.coerceIn(1, 50)))
        val agentName = entities.firstOrNull()?.let { resolveAgentName(it.agentId) } ?: ""
        val lbcName = entities.firstOrNull()?.let { resolveLbcName(it.lbcId) } ?: ""
        return entities.map { it.toDomain(agentName, lbcName) }
    }

    override fun findByIds(ids: List<UUID>): List<Farmer> {
        if (ids.isEmpty()) return emptyList()
        val entities = jpaRepository.findAllById(ids)
        val agentIds = entities.map { it.agentId }.distinct()
        val lbcIds = entities.map { it.lbcId }.distinct()
        val agentMap = agentJpaRepository.findAllById(agentIds).associateBy { it.id!! }
        val lbcMap = lbcJpaRepository.findAllById(lbcIds).associateBy { it.id!! }
        return entities.map { entity ->
            val agentName = agentMap[entity.agentId]?.let { "${it.firstName} ${it.lastName}" } ?: ""
            val lbcName = lbcMap[entity.lbcId]?.name ?: ""
            entity.toDomain(agentName, lbcName)
        }
    }

    private fun buildSpec(
        status: FarmerStatus?,
        region: String?,
        lbcId: UUID?,
        agentId: UUID?,
        kycVerified: Boolean?,
        search: String?
    ): Specification<FarmerJpaEntity> = Specification { root, query, cb ->
        val predicates = mutableListOf<Predicate>()

        status?.let { predicates.add(cb.equal(root.get<FarmerStatus>("status"), it)) }

        region?.takeIf { it.isNotBlank() }?.let {
            predicates.add(cb.equal(cb.lower(root.get("region")), it.lowercase()))
        }

        lbcId?.let { predicates.add(cb.equal(root.get<UUID>("lbcId"), it)) }

        agentId?.let { predicates.add(cb.equal(root.get<UUID>("agentId"), it)) }

        kycVerified?.let { predicates.add(cb.equal(root.get<Boolean>("kycVerified"), it)) }

        search?.takeIf { it.isNotBlank() }?.let { q ->
            val pattern = "%${q.lowercase()}%"
            val fullName = cb.concat(cb.concat(root.get("firstName"), " "), root.get("lastName"))
            predicates.add(
                cb.or(
                    cb.like(cb.lower(root.get("farmerCode")), pattern),
                    cb.like(cb.lower(root.get("firstName")), pattern),
                    cb.like(cb.lower(root.get("lastName")), pattern),
                    cb.like(cb.lower(fullName), pattern),
                    cb.like(cb.lower(root.get("phone")), pattern),
                    cb.like(cb.lower(root.get("email")), pattern),
                    cb.like(cb.lower(root.get("nationalId")), pattern)
                )
            )
            query?.distinct(true)
        }

        cb.and(*predicates.toTypedArray())
    }

    private fun resolveAgentName(agentId: UUID): String {
        val agent = agentJpaRepository.findById(agentId).orElse(null) ?: return ""
        return "${agent.firstName} ${agent.lastName}"
    }

    private fun resolveLbcName(lbcId: UUID): String =
        lbcJpaRepository.findById(lbcId).orElse(null)?.name ?: ""

    private fun toUUID(value: Any?): UUID? = when (value) {
        is UUID -> value
        is String -> runCatching { UUID.fromString(value) }.getOrNull()
        else -> null
    }

    override fun findRegionalOverview(): List<RegionalSummary> =
        jpaRepository.findRegionalOverview().map {
            RegionalSummary(
                region = it.getRegion(),
                farmers = it.getFarmers(),
                produce = it.getProduce().toDouble(),
                revenue = it.getRevenue()
            )
        }

    override fun findRecentGlobal(limit: Int): List<Farmer> =
        jpaRepository.findTop5ByOrderByCreatedAtDesc().take(limit).map { it.toDomain() }

    override fun countCreatedBetween(from: LocalDateTime, to: LocalDateTime): Long =
        jpaRepository.countByCreatedAtBetween(from, to)
}
