package com.soiltech.backend.domain.entity

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class Product(
    val id: UUID,
    val categoryId: UUID,
    val produceListingId: UUID?,
    val name: String,
    val description: String?,
    val pricePerUnit: BigDecimal,
    val unit: String,
    val stockQuantity: Int,
    val isAvailable: Boolean,
    val imageUrl: String?,
    val isOnDeal: Boolean,
    val isFeatured: Boolean,
    val originalPrice: BigDecimal?,
    val farmerName: String?,
    val location: String?,
    val freshnessLabel: String?,
    val averageRating: BigDecimal,
    val reviewCount: Int,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
