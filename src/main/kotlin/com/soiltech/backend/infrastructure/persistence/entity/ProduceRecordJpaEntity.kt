package com.soiltech.backend.infrastructure.persistence.entity

import com.soiltech.backend.domain.entity.ProduceRecord
import com.soiltech.backend.domain.enum.CollectionStatus
import com.soiltech.backend.domain.enum.SyncStatus
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(
    name = "produce_records",
    indexes = [
        Index(name = "idx_produce_farmer_id", columnList = "farmerId"),
        Index(name = "idx_produce_agent_id", columnList = "agentId"),
        Index(name = "idx_produce_status", columnList = "status")
    ]
)
class ProduceRecordJpaEntity(
    @Id
    val id: UUID? = null,

    @Column(nullable = false)
    val farmerId: UUID,

    val farmId: UUID? = null,

    @Column(nullable = false)
    val agentId: UUID,

    @Column(nullable = false, length = 100)
    var cropType: String,

    @Column(nullable = false, precision = 10, scale = 3)
    var quantityKg: BigDecimal,

    @Column(nullable = false, precision = 12, scale = 2)
    var pricePerKg: BigDecimal,

    @Column(nullable = false, precision = 14, scale = 2)
    var totalAmount: BigDecimal,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: CollectionStatus = CollectionStatus.PENDING,

    var collectedAt: LocalDateTime? = null,

    @Column(length = 1000)
    var notes: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var syncStatus: SyncStatus = SyncStatus.PENDING
) : BaseJpaEntity() {


    fun toDomain(): ProduceRecord = ProduceRecord(
        id = id!!,
        farmerId = farmerId,
        farmId = farmId,
        agentId = agentId,
        cropType = cropType,
        quantityKg = quantityKg,
        pricePerKg = pricePerKg,
        totalAmount = totalAmount,
        status = status,
        collectedAt = collectedAt,
        notes = notes,
        syncStatus = syncStatus,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(record: ProduceRecord): ProduceRecordJpaEntity = ProduceRecordJpaEntity(
            id = record.id,
            farmerId = record.farmerId,
            farmId = record.farmId,
            agentId = record.agentId,
            cropType = record.cropType,
            quantityKg = record.quantityKg,
            pricePerKg = record.pricePerKg,
            totalAmount = record.totalAmount,
            status = record.status,
            collectedAt = record.collectedAt,
            notes = record.notes,
            syncStatus = record.syncStatus
        )
    }
}
