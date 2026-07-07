package com.soiltech.backend.infrastructure.persistence.entity

import com.soiltech.backend.domain.entity.PickupRequest
import com.soiltech.backend.domain.enum.LogisticsStatus
import jakarta.persistence.*
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(
    name = "pickup_requests",
    indexes = [
        Index(name = "idx_pickup_farmer_id", columnList = "farmerId"),
        Index(name = "idx_pickup_agent_id", columnList = "agentId"),
        Index(name = "idx_pickup_status", columnList = "status")
    ]
)
class PickupRequestJpaEntity(
    @Id
    val id: UUID? = null,

    @Column(nullable = false)
    val farmerId: UUID,

    @Column(nullable = false)
    val agentId: UUID,

    val produceRecordId: UUID? = null,

    @Column(nullable = false)
    var scheduledDate: LocalDate,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: LogisticsStatus = LogisticsStatus.PENDING,

    @Column(length = 1000)
    var notes: String? = null
) : BaseJpaEntity() {


    fun toDomain(): PickupRequest = PickupRequest(
        id = id!!,
        farmerId = farmerId,
        agentId = agentId,
        produceRecordId = produceRecordId,
        scheduledDate = scheduledDate,
        status = status,
        notes = notes,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(request: PickupRequest): PickupRequestJpaEntity = PickupRequestJpaEntity(
            id = request.id,
            farmerId = request.farmerId,
            agentId = request.agentId,
            produceRecordId = request.produceRecordId,
            scheduledDate = request.scheduledDate,
            status = request.status,
            notes = request.notes
        )
    }
}
