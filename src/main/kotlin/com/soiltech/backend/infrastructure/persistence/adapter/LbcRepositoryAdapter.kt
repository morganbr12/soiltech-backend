package com.soiltech.backend.infrastructure.persistence.adapter

import com.soiltech.backend.domain.entity.Lbc
import com.soiltech.backend.domain.enum.LbcStatus
import com.soiltech.backend.domain.repository.LbcRepository
import com.soiltech.backend.infrastructure.persistence.entity.LbcJpaEntity
import com.soiltech.backend.infrastructure.persistence.jpa.LbcJpaRepository
import jakarta.persistence.criteria.Predicate
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class LbcRepositoryAdapter(
    private val jpaRepository: LbcJpaRepository
) : LbcRepository {

    override fun findById(id: UUID): Lbc? =
        jpaRepository.findById(id).orElse(null)?.toDomain()

    override fun findByCode(code: String): Lbc? =
        jpaRepository.findByCode(code)?.toDomain()

    override fun findByEmail(email: String): Lbc? =
        jpaRepository.findByEmail(email)?.toDomain()

    override fun findAll(status: LbcStatus?, region: String?, search: String?, pageable: Pageable): Page<Lbc> =
        jpaRepository.findAll(buildSpec(status, region, search), pageable).map { it.toDomain() }

    override fun countByStatus(): Map<LbcStatus, Long> {
        val result = mutableMapOf<LbcStatus, Long>()
        LbcStatus.entries.forEach { result[it] = 0L }
        jpaRepository.countGroupByStatus().forEach { row ->
            val status = when (val raw = row[0]) {
                is LbcStatus -> raw
                is String -> LbcStatus.entries.firstOrNull { it.name == raw || it.value == raw }
                else -> null
            }
            val count = (row[1] as? Long) ?: (row[1] as? Number)?.toLong() ?: 0L
            if (status != null) result[status] = count
        }
        return result
    }

    override fun findAllForExport(status: LbcStatus?, region: String?, ids: List<UUID>?): List<Lbc> {
        val spec = buildSpec(status, region, null).and { root, _, cb ->
            if (!ids.isNullOrEmpty()) root.get<UUID>("id").`in`(ids) else cb.conjunction()
        }
        return jpaRepository.findAll(spec).map { it.toDomain() }
    }

    override fun save(lbc: Lbc): Lbc {
        val existing = jpaRepository.findById(lbc.id).orElse(null)
        return if (existing != null) {
            existing.apply {
                userId = lbc.userId
                name = lbc.name
                code = lbc.code
                region = lbc.region
                district = lbc.district
                manager = lbc.manager
                phone = lbc.phone
                email = lbc.email
                agents = lbc.agents
                farmers = lbc.farmers
                produceTonnes = lbc.produceTonnes
                revenue = lbc.revenue
                compliance = lbc.compliance
                status = lbc.status
                joinedDate = lbc.joinedDate
            }
            jpaRepository.save(existing).toDomain()
        } else {
            jpaRepository.save(LbcJpaEntity.fromDomain(lbc)).toDomain()
        }
    }

    override fun delete(id: UUID) =
        jpaRepository.deleteById(id)

    override fun existsByCode(code: String): Boolean =
        jpaRepository.existsByCode(code)

    override fun existsByEmail(email: String): Boolean =
        jpaRepository.existsByEmail(email)

    override fun existsByCodeAndIdNot(code: String, id: UUID): Boolean =
        jpaRepository.existsByCodeAndIdNot(code, id)

    override fun existsByEmailAndIdNot(email: String, id: UUID): Boolean =
        jpaRepository.existsByEmailAndIdNot(email, id)

    private fun buildSpec(status: LbcStatus?, region: String?, search: String?): Specification<LbcJpaEntity> =
        Specification { root, _, cb ->
            val predicates = mutableListOf<Predicate>()

            status?.let { predicates.add(cb.equal(root.get<LbcStatus>("status"), it)) }

            region?.takeIf { it.isNotBlank() }?.let {
                predicates.add(cb.equal(cb.lower(root.get("region")), it.lowercase()))
            }

            search?.takeIf { it.isNotBlank() }?.let { q ->
                val pattern = "%${q.lowercase()}%"
                predicates.add(
                    cb.or(
                        cb.like(cb.lower(root.get("name")), pattern),
                        cb.like(cb.lower(root.get("code")), pattern),
                        cb.like(cb.lower(root.get("region")), pattern)
                    )
                )
            }

            cb.and(*predicates.toTypedArray())
        }
}
