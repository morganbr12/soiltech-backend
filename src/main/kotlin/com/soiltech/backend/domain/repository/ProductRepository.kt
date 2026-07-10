package com.soiltech.backend.domain.repository

import com.soiltech.backend.domain.entity.Product
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface ProductRepository {
    fun findById(id: UUID): Product?
    fun findByProduceListingId(listingId: UUID): Product?
    fun findAll(categoryId: UUID?, query: String?, pageable: Pageable): Page<Product>
    fun findDeals(pageable: Pageable): Page<Product>
    fun findFeatured(pageable: Pageable): Page<Product>
    fun save(product: Product): Product
    fun backfillFarmerAgentIds(): Int
}
