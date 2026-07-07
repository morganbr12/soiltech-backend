package com.soiltech.backend.domain.entity

import java.time.LocalDateTime
import java.util.UUID

data class ProductReview(
    val id: UUID,
    val productId: UUID,
    val customerId: UUID,
    val rating: Int,
    val comment: String?,
    val createdAt: LocalDateTime
)
