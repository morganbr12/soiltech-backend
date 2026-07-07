package com.soiltech.backend.infrastructure.persistence.entity

import com.soiltech.backend.domain.entity.ProductCategory
import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "product_categories")
class ProductCategoryJpaEntity(
    @Id
    val id: UUID? = null,

    @Column(nullable = false, unique = true, length = 100)
    var name: String,

    @Column(length = 500)
    var description: String? = null
) : BaseJpaEntity() {


    fun toDomain(): ProductCategory = ProductCategory(
        id = id!!,
        name = name,
        description = description,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
