package com.soiltech.backend.infrastructure.persistence.entity

import com.soiltech.backend.domain.entity.ProductReview
import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(
    name = "product_reviews",
    indexes = [
        Index(name = "idx_reviews_product_id", columnList = "productId"),
        Index(name = "idx_reviews_customer_id", columnList = "customerId")
    ]
)
class ProductReviewJpaEntity(
    @Id
    val id: UUID? = null,

    @Column(nullable = false)
    val productId: UUID,

    @Column(nullable = false)
    val customerId: UUID,

    @Column(nullable = false)
    val rating: Int,

    @Column(length = 2000)
    val comment: String? = null
) : BaseJpaEntity() {


    fun toDomain(): ProductReview = ProductReview(
        id = id!!,
        productId = productId,
        customerId = customerId,
        rating = rating,
        comment = comment,
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(review: ProductReview): ProductReviewJpaEntity = ProductReviewJpaEntity(
            id = review.id,
            productId = review.productId,
            customerId = review.customerId,
            rating = review.rating,
            comment = review.comment
        )
    }
}
