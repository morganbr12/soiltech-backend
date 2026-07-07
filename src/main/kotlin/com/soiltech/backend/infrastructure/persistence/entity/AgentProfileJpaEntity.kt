package com.soiltech.backend.infrastructure.persistence.entity

import com.soiltech.backend.domain.entity.AgentProfile
import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(
    name = "agent_profiles",
    indexes = [Index(name = "idx_agent_profiles_user_id", columnList = "userId", unique = true)]
)
class AgentProfileJpaEntity(
    @Id
    val id: UUID? = null,

    @Column(nullable = false)
    val userId: UUID,

    @Column(nullable = false, length = 255)
    var fullName: String,

    @Column(nullable = false, unique = true, length = 50)
    var agentCode: String,

    @Column(length = 100)
    var region: String? = null
) : BaseJpaEntity() {


    fun toDomain(): AgentProfile = AgentProfile(
        id = id!!,
        userId = userId,
        fullName = fullName,
        agentCode = agentCode,
        region = region,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(profile: AgentProfile): AgentProfileJpaEntity = AgentProfileJpaEntity(
            id = profile.id,
            userId = profile.userId,
            fullName = profile.fullName,
            agentCode = profile.agentCode,
            region = profile.region
        )
    }
}
