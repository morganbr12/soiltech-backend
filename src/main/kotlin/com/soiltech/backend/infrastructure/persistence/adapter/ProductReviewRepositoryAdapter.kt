package com.soiltech.backend.infrastructure.persistence.adapter

import com.soiltech.backend.domain.entity.ProductReview
import com.soiltech.backend.domain.repository.ProductReviewRepository
import com.soiltech.backend.infrastructure.persistence.entity.ProductReviewJpaEntity
import com.soiltech.backend.infrastructure.persistence.jpa.ProductReviewJpaRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class ProductReviewRepositoryAdapter(
    private val jpaRepository: ProductReviewJpaRepository
) : ProductReviewRepository {

    override fun findByProductId(productId: UUID, pageable: Pageable): Page<ProductReview> =
        jpaRepository.findByProductId(productId, pageable).map { it.toDomain() }

    override fun save(review: ProductReview): ProductReview =
        jpaRepository.save(ProductReviewJpaEntity.fromDomain(review)).toDomain()

    override fun existsByProductIdAndCustomerId(productId: UUID, customerId: UUID): Boolean =
        jpaRepository.existsByProductIdAndCustomerId(productId, customerId)
}
