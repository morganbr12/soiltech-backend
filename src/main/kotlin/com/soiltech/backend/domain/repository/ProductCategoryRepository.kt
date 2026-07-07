package com.soiltech.backend.domain.repository

import com.soiltech.backend.domain.entity.ProductCategory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface ProductCategoryRepository {
    fun findById(id: UUID): ProductCategory?
    fun findAll(pageable: Pageable): Page<ProductCategory>
}
