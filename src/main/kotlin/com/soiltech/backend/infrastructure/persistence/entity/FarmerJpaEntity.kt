package com.soiltech.backend.infrastructure.persistence.entity

import com.soiltech.backend.domain.entity.Farmer
import com.soiltech.backend.domain.enum.FarmerStatus
import com.soiltech.backend.domain.enum.SyncStatus
import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(
    name = "farmers",
    indexes = [
        Index(name = "idx_farmers_agent_id", columnList = "agentId"),
        Index(name = "idx_farmers_status", columnList = "status")
    ]
)
class FarmerJpaEntity(
    @Id
    val id: UUID? = null,

    @Column(nullable = false)
    val agentId: UUID,

    @Column(nullable = false, length = 255)
    var fullName: String,

    @Column(nullable = false, length = 20)
    var phone: String,

    @Column(length = 50)
    var nationalId: String? = null,

    @Column(length = 500)
    var location: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: FarmerStatus = FarmerStatus.ACTIVE,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var syncStatus: SyncStatus = SyncStatus.PENDING
) : BaseJpaEntity() {


    fun toDomain(): Farmer = Farmer(
        id = id!!,
        agentId = agentId,
        fullName = fullName,
        phone = phone,
        nationalId = nationalId,
        location = location,
        status = status,
        syncStatus = syncStatus,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(farmer: Farmer): FarmerJpaEntity = FarmerJpaEntity(
            id = farmer.id,
            agentId = farmer.agentId,
            fullName = farmer.fullName,
            phone = farmer.phone,
            nationalId = farmer.nationalId,
            location = farmer.location,
            status = farmer.status,
            syncStatus = farmer.syncStatus
        )
    }
}
