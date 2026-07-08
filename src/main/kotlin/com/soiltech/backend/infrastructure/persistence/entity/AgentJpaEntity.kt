package com.soiltech.backend.infrastructure.persistence.entity

import com.soiltech.backend.domain.entity.Agent
import com.soiltech.backend.domain.enum.AgentStatus
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(
    name = "agents",
    indexes = [
        Index(name = "idx_agents_email", columnList = "email", unique = true),
        Index(name = "idx_agents_phone", columnList = "phone", unique = true),
        Index(name = "idx_agents_agent_code", columnList = "agentCode", unique = true),
        Index(name = "idx_agents_lbc_id", columnList = "lbc_id"),
        Index(name = "idx_agents_status", columnList = "status"),
        Index(name = "idx_agents_region", columnList = "region")
    ]
)
class AgentJpaEntity(
    @Id
    val id: UUID? = null,

    @Column(nullable = false, length = 100)
    var firstName: String,

    @Column(nullable = false, length = 100)
    var lastName: String,

    @Column(nullable = false, unique = true, length = 20)
    var phone: String,

    @Column(nullable = false, unique = true, length = 255)
    var email: String,

    @Column(nullable = false, unique = true, length = 50)
    var agentCode: String,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "lbc_id", nullable = false)
    var lbc: LbcJpaEntity,

    @Column(nullable = false, length = 100)
    var region: String,

    @Column(nullable = false, length = 100)
    var district: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20)")
    var status: AgentStatus = AgentStatus.ACTIVE,

    @Column
    var latitude: Double? = null,

    @Column
    var longitude: Double? = null,

    @Column
    var lastSeen: LocalDateTime? = null,

    @Column(nullable = false)
    var joinedDate: LocalDateTime = LocalDateTime.now()

) : BaseJpaEntity() {

    fun toDomain(): Agent = Agent(
        id = id!!,
        firstName = firstName,
        lastName = lastName,
        phone = phone,
        email = email,
        agentCode = agentCode,
        lbcId = lbc.id!!,
        lbcName = lbc.name,
        region = region,
        district = district,
        status = status,
        latitude = latitude,
        longitude = longitude,
        lastSeen = lastSeen,
        joinedDate = joinedDate,
        createdAt = createdAt,
        updatedAt = updatedAt,
        createdBy = createdBy,
        updatedBy = updatedBy
    )
}
