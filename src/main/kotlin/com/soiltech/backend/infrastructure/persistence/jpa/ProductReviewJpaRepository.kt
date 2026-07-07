package com.soiltech.backend.infrastructure.persistence.jpa

import com.soiltech.backend.infrastructure.persistence.entity.ProductReviewJpaEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ProductReviewJpaRepository : JpaRepository<ProductReviewJpaEntity, UUID> {
    fun findByProductId(productId: UUID, pageable: Pageable): Page<ProductReviewJpaEntity>
    fun existsByProductIdAndCustomerId(productId: UUID, customerId: UUID): Boolean
    fun countByProductId(productId: UUID): Long

    @Query("SELECT COALESCE(AVG(CAST(r.rating AS double)), 0.0) FROM ProductReviewJpaEntity r WHERE r.productId = :productId")
    fun findAverageRatingByProductId(@Param("productId") productId: UUID): Double
}
