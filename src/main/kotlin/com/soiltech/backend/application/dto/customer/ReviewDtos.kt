package com.soiltech.backend.application.dto.customer

import com.soiltech.backend.domain.enum.ReviewStatus
import com.soiltech.backend.domain.enum.ReviewTargetType
import java.time.LocalDateTime
import java.util.UUID

data class ReviewResponse(
    val id: UUID,
    val customerId: UUID,
    val customerName: String,
    val targetType: ReviewTargetType,
    val targetId: String,
    val targetName: String,
    val rating: Int,
    val comment: String?,
    val status: ReviewStatus,
    val region: String?,
    val createdAt: LocalDateTime
)

data class ReviewSummaryResponse(
    val total: Long,
    val approved: Long,
    val pending: Long,
    val flagged: Long,
    val rejected: Long,
    val avgRating: Double
)

data class FlagReviewRequest(val reason: String? = null)
