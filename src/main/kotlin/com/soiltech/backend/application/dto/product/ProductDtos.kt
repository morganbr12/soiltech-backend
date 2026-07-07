package com.soiltech.backend.application.dto.product

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class ProductCategoryDto(
    val id: UUID,
    val name: String,
    val description: String?,
    val createdAt: LocalDateTime
)

data class ProductDto(
    val id: UUID,
    val categoryId: UUID,
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

data class ProductReviewDto(
    val id: UUID,
    val productId: UUID,
    val customerId: UUID,
    val rating: Int,
    val comment: String?,
    val createdAt: LocalDateTime
)

data class CreateProductReviewRequest(
    val rating: Int,
    val comment: String? = null
)
