package com.soiltech.backend.infrastructure.persistence.entity

import com.soiltech.backend.domain.entity.KycDocument
import com.soiltech.backend.domain.enum.KycDocumentType
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(
    name = "kyc_documents",
    indexes = [Index(name = "idx_kyc_docs_customer_id", columnList = "customerId")]
)
class KycDocumentJpaEntity(
    @Id
    val id: UUID? = null,

    @Column(nullable = false)
    val customerId: UUID,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(30)")
    val type: KycDocumentType,

    @Column(nullable = false, length = 1000)
    var url: String,

    @Column(nullable = false, updatable = false)
    val uploadedAt: LocalDateTime = LocalDateTime.now()

) {
    fun toDomain(): KycDocument = KycDocument(
        id = id!!,
        customerId = customerId,
        type = type,
        url = url,
        uploadedAt = uploadedAt
    )

    companion object {
        fun fromDomain(d: KycDocument): KycDocumentJpaEntity = KycDocumentJpaEntity(
            id = d.id,
            customerId = d.customerId,
            type = d.type,
            url = d.url,
            uploadedAt = d.uploadedAt
        )
    }
}
