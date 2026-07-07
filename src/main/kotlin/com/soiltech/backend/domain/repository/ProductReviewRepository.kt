package com.soiltech.backend.domain.repository

import com.soiltech.backend.domain.entity.ProductReview
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface ProductReviewRepository {
    fun findByProductId(productId: UUID, pageable: Pageable): Page<ProductReview>
    fun save(review: ProductReview): ProductReview
    fun existsByProductIdAndCustomerId(productId: UUID, customerId: UUID): Boolean
}
