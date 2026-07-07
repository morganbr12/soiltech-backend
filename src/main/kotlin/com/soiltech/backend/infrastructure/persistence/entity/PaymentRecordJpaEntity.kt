package com.soiltech.backend.infrastructure.persistence.entity

import com.soiltech.backend.domain.entity.PaymentRecord
import com.soiltech.backend.domain.enum.PaymentStatus
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(
    name = "payment_records",
    indexes = [
        Index(name = "idx_payment_farmer_id", columnList = "farmerId"),
        Index(name = "idx_payment_agent_id", columnList = "agentId"),
        Index(name = "idx_payment_status", columnList = "status")
    ]
)
class PaymentRecordJpaEntity(
    @Id
    val id: UUID? = null,

    @Column(nullable = false)
    val farmerId: UUID,

    @Column(nullable = false)
    val agentId: UUID,

    val produceRecordId: UUID? = null,

    @Column(nullable = false, precision = 14, scale = 2)
    var amount: BigDecimal,

    @Column(nullable = false, length = 3)
    var currency: String = "KES",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: PaymentStatus = PaymentStatus.PENDING,

    @Column(length = 100)
    var reference: String? = null,

    var paidAt: LocalDateTime? = null
) : BaseJpaEntity() {


    fun toDomain(): PaymentRecord = PaymentRecord(
        id = id!!,
        farmerId = farmerId,
        agentId = agentId,
        produceRecordId = produceRecordId,
        amount = amount,
        currency = currency,
        status = status,
        reference = reference,
        paidAt = paidAt,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(record: PaymentRecord): PaymentRecordJpaEntity = PaymentRecordJpaEntity(
            id = record.id,
            farmerId = record.farmerId,
            agentId = record.agentId,
            produceRecordId = record.produceRecordId,
            amount = record.amount,
            currency = record.currency,
            status = record.status,
            reference = record.reference,
            paidAt = record.paidAt
        )
    }
}
