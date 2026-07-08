package com.soiltech.backend.domain.repository

import com.soiltech.backend.domain.entity.CustomerReview
import com.soiltech.backend.domain.enum.ReviewStatus
import com.soiltech.backend.domain.enum.ReviewTargetType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface CustomerReviewRepository {
    fun findById(id: java.util.UUID): CustomerReview?
    fun findAll(
        status: ReviewStatus?,
        targetType: ReviewTargetType?,
        region: String?,
        search: String?,
        pageable: Pageable
    ): Page<CustomerReview>
    fun save(review: CustomerReview): CustomerReview
    fun delete(id: java.util.UUID)
    fun countByStatus(): Map<ReviewStatus, Long>
    fun avgRating(): Double
}
