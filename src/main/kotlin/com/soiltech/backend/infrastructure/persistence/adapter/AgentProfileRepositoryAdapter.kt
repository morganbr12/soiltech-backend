package com.soiltech.backend.infrastructure.persistence.adapter

import com.soiltech.backend.domain.entity.AgentProfile
import com.soiltech.backend.domain.repository.AgentProfileRepository
import com.soiltech.backend.infrastructure.persistence.entity.AgentProfileJpaEntity
import com.soiltech.backend.infrastructure.persistence.jpa.AgentProfileJpaRepository
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class AgentProfileRepositoryAdapter(
    private val jpaRepository: AgentProfileJpaRepository
) : AgentProfileRepository {

    override fun findById(id: UUID): AgentProfile? =
        jpaRepository.findById(id).orElse(null)?.toDomain()

    override fun findByUserId(userId: UUID): AgentProfile? =
        jpaRepository.findByUserId(userId)?.toDomain()

    override fun save(profile: AgentProfile): AgentProfile {
        val entity = jpaRepository.findById(profile.id)
            .orElse(AgentProfileJpaEntity.fromDomain(profile))
        entity.apply {
            fullName = profile.fullName
            region = profile.region
        }
        return jpaRepository.save(entity).toDomain()
    }
}
