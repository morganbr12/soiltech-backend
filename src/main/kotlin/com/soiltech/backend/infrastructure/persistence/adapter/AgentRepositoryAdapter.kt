package com.soiltech.backend.infrastructure.persistence.adapter

import com.soiltech.backend.domain.entity.Agent
import com.soiltech.backend.domain.entity.AgentMetrics
import com.soiltech.backend.domain.enum.AgentStatus
import com.soiltech.backend.domain.repository.AgentRepository
import com.soiltech.backend.infrastructure.persistence.entity.AgentJpaEntity
import com.soiltech.backend.infrastructure.persistence.jpa.AgentJpaRepository
import com.soiltech.backend.infrastructure.persistence.jpa.LbcJpaRepository
import com.soiltech.backend.interfaces.exception.NotFoundException
import jakarta.persistence.criteria.JoinType
import jakarta.persistence.criteria.Predicate
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Component
class AgentRepositoryAdapter(
    private val jpaRepository: AgentJpaRepository,
    private val lbcJpaRepository: LbcJpaRepository
) : AgentRepository {

    override fun findById(id: UUID): Agent? =
        jpaRepository.findById(id).orElse(null)?.toDomain()

    override fun findByIds(ids: List<UUID>): Map<UUID, Agent> {
        if (ids.isEmpty()) return emptyMap()
        return jpaRepository.findAllById(ids)
            .filter { it.id != null }
            .associate { it.id!! to it.toDomain() }
    }

    override fun findAll(status: AgentStatus?, region: String?, search: String?, pageable: Pageable): Page<Agent> =
        jpaRepository.findAll(buildSpec(status, region, search), pageable).map { it.toDomain() }

    override fun countByStatus(): Map<AgentStatus, Long> {
        val result = mutableMapOf<AgentStatus, Long>()
        AgentStatus.entries.forEach { result[it] = 0L }
        jpaRepository.countGroupByStatus().forEach { row ->
            val status = when (val raw = row[0]) {
                is AgentStatus -> raw
                is String -> AgentStatus.entries.firstOrNull { it.name == raw || it.value == raw }
                else -> null
            }
            val count = (row[1] as? Long) ?: (row[1] as? Number)?.toLong() ?: 0L
            if (status != null) result[status] = count
        }
        return result
    }

    override fun findMetricsByAgentIds(ids: List<UUID>): Map<UUID, AgentMetrics> {
        if (ids.isEmpty()) return emptyMap()

        val metricsMap = mutableMapOf<UUID, AgentMetrics>()
        ids.forEach { metricsMap[it] = AgentMetrics() }

        jpaRepository.countFarmersByAgentIds(ids).forEach { row ->
            val agentId = toUUID(row[0]) ?: return@forEach
            val count = (row[1] as? Number)?.toLong() ?: 0L
            metricsMap[agentId] = (metricsMap[agentId] ?: AgentMetrics()).copy(farmersCount = count)
        }

        jpaRepository.countFarmsByAgentIds(ids).forEach { row ->
            val agentId = toUUID(row[0]) ?: return@forEach
            val count = (row[1] as? Number)?.toLong() ?: 0L
            metricsMap[agentId] = (metricsMap[agentId] ?: AgentMetrics()).copy(farmsCount = count)
        }

        jpaRepository.sumProduceByAgentIds(ids).forEach { row ->
            val agentId = toUUID(row[0]) ?: return@forEach
            val sum = when (val raw = row[1]) {
                is BigDecimal -> raw
                is Number -> BigDecimal(raw.toString())
                else -> BigDecimal.ZERO
            }
            metricsMap[agentId] = (metricsMap[agentId] ?: AgentMetrics()).copy(produceCollected = sum)
        }

        return metricsMap
    }

    override fun save(agent: Agent): Agent {
        val lbcEntity = lbcJpaRepository.findById(agent.lbcId).orElseThrow {
            NotFoundException("LBC with ID '${agent.lbcId}' not found")
        }

        val existing = jpaRepository.findById(agent.id).orElse(null)
        return if (existing != null) {
            existing.apply {
                firstName = agent.firstName
                lastName = agent.lastName
                phone = agent.phone
                email = agent.email
                lbc = lbcEntity
                region = agent.region
                district = agent.district
                status = agent.status
                latitude = agent.latitude
                longitude = agent.longitude
                lastSeen = agent.lastSeen
                joinedDate = agent.joinedDate
            }
            jpaRepository.save(existing).toDomain()
        } else {
            jpaRepository.save(
                AgentJpaEntity(
                    id = agent.id,
                    firstName = agent.firstName,
                    lastName = agent.lastName,
                    phone = agent.phone,
                    email = agent.email,
                    agentCode = agent.agentCode,
                    lbc = lbcEntity,
                    region = agent.region,
                    district = agent.district,
                    status = agent.status,
                    latitude = agent.latitude,
                    longitude = agent.longitude,
                    lastSeen = agent.lastSeen,
                    joinedDate = agent.joinedDate
                )
            ).toDomain()
        }
    }

    override fun delete(id: UUID) = jpaRepository.deleteById(id)

    override fun existsByEmail(email: String): Boolean = jpaRepository.existsByEmail(email)

    override fun existsByPhone(phone: String): Boolean = jpaRepository.existsByPhone(phone)

    override fun existsByEmailAndIdNot(email: String, id: UUID): Boolean =
        jpaRepository.existsByEmailAndIdNot(email, id)

    override fun existsByPhoneAndIdNot(phone: String, id: UUID): Boolean =
        jpaRepository.existsByPhoneAndIdNot(phone, id)

    override fun existsByAgentCode(code: String): Boolean = jpaRepository.existsByAgentCode(code)

    override fun findByAgentCode(agentCode: String): Agent? =
        jpaRepository.findByAgentCode(agentCode)?.toDomain()

    private fun buildSpec(status: AgentStatus?, region: String?, search: String?): Specification<AgentJpaEntity> =
        Specification { root, query, cb ->
            val predicates = mutableListOf<Predicate>()

            status?.let { predicates.add(cb.equal(root.get<AgentStatus>("status"), it)) }

            region?.takeIf { it.isNotBlank() }?.let {
                predicates.add(cb.equal(cb.lower(root.get("region")), it.lowercase()))
            }

            search?.takeIf { it.isNotBlank() }?.let { q ->
                val pattern = "%${q.lowercase()}%"
                val lbcJoin = root.join<AgentJpaEntity, Any>("lbc", JoinType.LEFT)
                val fullName = cb.concat(cb.concat(root.get("firstName"), " "), root.get("lastName"))
                predicates.add(
                    cb.or(
                        cb.like(cb.lower(root.get("firstName")), pattern),
                        cb.like(cb.lower(root.get("lastName")), pattern),
                        cb.like(cb.lower(fullName), pattern),
                        cb.like(cb.lower(root.get("email")), pattern),
                        cb.like(cb.lower(root.get("phone")), pattern),
                        cb.like(cb.lower(lbcJoin.get("name")), pattern),
                        cb.like(cb.lower(root.get("region")), pattern)
                    )
                )
                query?.distinct(true)
            }

            cb.and(*predicates.toTypedArray())
        }

    private fun toUUID(value: Any?): UUID? = when (value) {
        is UUID -> value
        is String -> runCatching { UUID.fromString(value) }.getOrNull()
        else -> null
    }

    override fun countCreatedBetween(from: LocalDateTime, to: LocalDateTime): Long =
        jpaRepository.countByCreatedAtBetween(from, to)
}
