package com.soiltech.backend.infrastructure.persistence.entity

import com.soiltech.backend.domain.entity.CustomerReview
import com.soiltech.backend.domain.enum.ReviewStatus
import com.soiltech.backend.domain.enum.ReviewTargetType
import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(
    name = "customer_reviews",
    indexes = [
        Index(name = "idx_reviews_customer_id", columnList = "customerId"),
        Index(name = "idx_reviews_status", columnList = "status"),
        Index(name = "idx_reviews_target_type", columnList = "targetType")
    ]
)
class CustomerReviewJpaEntity(
    @Id
    val id: UUID? = null,

    @Column(nullable = false)
    val customerId: UUID,

    @Column(nullable = false, length = 255)
    var customerName: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20)")
    var targetType: ReviewTargetType,

    @Column(nullable = false, length = 100)
    var targetId: String,

    @Column(nullable = false, length = 255)
    var targetName: String,

    @Column(nullable = false)
    var rating: Int,

    @Column(length = 2000)
    var comment: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20)")
    var status: ReviewStatus = ReviewStatus.PENDING,

    @Column(length = 100)
    var region: String? = null,

    @Column(length = 1000)
    var flagReason: String? = null

) : BaseJpaEntity() {

    fun toDomain(): CustomerReview = CustomerReview(
        id = id!!,
        customerId = customerId,
        customerName = customerName,
        targetType = targetType,
        targetId = targetId,
        targetName = targetName,
        rating = rating,
        comment = comment,
        status = status,
        region = region,
        flagReason = flagReason,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(r: CustomerReview): CustomerReviewJpaEntity = CustomerReviewJpaEntity(
            id = r.id,
            customerId = r.customerId,
            customerName = r.customerName,
            targetType = r.targetType,
            targetId = r.targetId,
            targetName = r.targetName,
            rating = r.rating,
            comment = r.comment,
            status = r.status,
            region = r.region,
            flagReason = r.flagReason
        )
    }
}
