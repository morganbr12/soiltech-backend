package com.soiltech.backend.infrastructure.persistence.entity

import com.soiltech.backend.domain.entity.Product
import jakarta.persistence.*
import java.math.BigDecimal
import java.util.UUID

@Entity
@Table(
    name = "products",
    indexes = [
        Index(name = "idx_products_category_id", columnList = "categoryId"),
        Index(name = "idx_products_available", columnList = "isAvailable"),
        Index(name = "idx_products_on_deal", columnList = "isOnDeal"),
        Index(name = "idx_products_featured", columnList = "isFeatured")
    ]
)
class ProductJpaEntity(
    @Id
    val id: UUID? = null,

    @Column(nullable = false)
    val categoryId: UUID,

    @Column(nullable = false, length = 255)
    var name: String,

    @Column(length = 2000)
    var description: String? = null,

    @Column(nullable = false, precision = 12, scale = 2)
    var pricePerUnit: BigDecimal,

    @Column(nullable = false, length = 50)
    var unit: String,

    @Column(nullable = false)
    var stockQuantity: Int = 0,

    @Column(nullable = false)
    var isAvailable: Boolean = true,

    @Column(length = 1000)
    var imageUrl: String? = null,

    @Column(nullable = false)
    var isOnDeal: Boolean = false,

    @Column(nullable = false)
    var isFeatured: Boolean = false,

    @Column(precision = 12, scale = 2)
    var originalPrice: BigDecimal? = null,

    @Column(length = 255)
    var farmerName: String? = null,

    @Column(length = 255)
    var location: String? = null,

    @Column(length = 100)
    var freshnessLabel: String? = null,

    @Column(nullable = false, precision = 3, scale = 1)
    var averageRating: BigDecimal = BigDecimal.ZERO,

    @Column(nullable = false)
    var reviewCount: Int = 0
) : BaseJpaEntity() {


    fun toDomain(): Product = Product(
        id = id!!,
        categoryId = categoryId,
        name = name,
        description = description,
        pricePerUnit = pricePerUnit,
        unit = unit,
        stockQuantity = stockQuantity,
        isAvailable = isAvailable,
        imageUrl = imageUrl,
        isOnDeal = isOnDeal,
        isFeatured = isFeatured,
        originalPrice = originalPrice,
        farmerName = farmerName,
        location = location,
        freshnessLabel = freshnessLabel,
        averageRating = averageRating,
        reviewCount = reviewCount,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(product: Product): ProductJpaEntity = ProductJpaEntity(
            id = product.id,
            categoryId = product.categoryId,
            name = product.name,
            description = product.description,
            pricePerUnit = product.pricePerUnit,
            unit = product.unit,
            stockQuantity = product.stockQuantity,
            isAvailable = product.isAvailable,
            imageUrl = product.imageUrl,
            isOnDeal = product.isOnDeal,
            isFeatured = product.isFeatured,
            originalPrice = product.originalPrice,
            farmerName = product.farmerName,
            location = product.location,
            freshnessLabel = product.freshnessLabel,
            averageRating = product.averageRating,
            reviewCount = product.reviewCount
        )
    }
}
