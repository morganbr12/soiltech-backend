package com.soiltech.backend.domain.entity

import com.soiltech.backend.domain.enum.ReviewStatus
import com.soiltech.backend.domain.enum.ReviewTargetType
import java.time.LocalDateTime
import java.util.UUID

data class CustomerReview(
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
    val flagReason: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
