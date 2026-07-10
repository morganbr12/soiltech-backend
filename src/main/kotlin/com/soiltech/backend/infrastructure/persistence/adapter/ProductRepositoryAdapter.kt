package com.soiltech.backend.infrastructure.persistence.adapter

import com.soiltech.backend.domain.entity.Product
import com.soiltech.backend.domain.entity.ProductCategory
import com.soiltech.backend.domain.repository.ProductCategoryRepository
import com.soiltech.backend.domain.repository.ProductRepository
import com.soiltech.backend.infrastructure.persistence.entity.ProductCategoryJpaEntity
import com.soiltech.backend.infrastructure.persistence.jpa.ProductCategoryJpaRepository
import com.soiltech.backend.infrastructure.persistence.jpa.ProductJpaRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Component
class ProductRepositoryAdapter(
    private val jpaRepository: ProductJpaRepository
) : ProductRepository {

    override fun findById(id: UUID): Product? =
        jpaRepository.findById(id).orElse(null)?.toDomain()

    override fun findByProduceListingId(listingId: UUID): Product? =
        jpaRepository.findByProduceListingId(listingId)?.toDomain()

    override fun findAll(categoryId: UUID?, query: String?, pageable: Pageable): Page<Product> =
        jpaRepository.findAllFiltered(categoryId, query, pageable).map { it.toDomain() }

    override fun findDeals(pageable: Pageable): Page<Product> =
        jpaRepository.findByIsOnDealTrueAndIsAvailableTrue(pageable).map { it.toDomain() }

    override fun findFeatured(pageable: Pageable): Page<Product> =
        jpaRepository.findByIsFeaturedTrueAndIsAvailableTrue(pageable).map { it.toDomain() }

    @Transactional
    override fun backfillFarmerAgentIds(): Int = jpaRepository.backfillFarmerAgentIds()

    override fun save(product: Product): Product {
        val existing = jpaRepository.findById(product.id).orElse(null)
        return if (existing == null) {
            jpaRepository.save(com.soiltech.backend.infrastructure.persistence.entity.ProductJpaEntity.fromDomain(product)).toDomain()
        } else {
            existing.apply {
                name = product.name
                description = product.description
                pricePerUnit = product.pricePerUnit
                unit = product.unit
                stockQuantity = product.stockQuantity
                isAvailable = product.isAvailable
                imageUrl = product.imageUrl
                isOnDeal = product.isOnDeal
                isFeatured = product.isFeatured
                originalPrice = product.originalPrice
                farmerName = product.farmerName
                location = product.location
                freshnessLabel = product.freshnessLabel
                averageRating = product.averageRating
                reviewCount = product.reviewCount
                produceListingId = product.produceListingId
                farmerId = product.farmerId
                agentId = product.agentId
            }
            jpaRepository.save(existing).toDomain()
        }
    }
}

@Component
class ProductCategoryRepositoryAdapter(
    private val jpaRepository: ProductCategoryJpaRepository
) : ProductCategoryRepository {

    override fun findById(id: UUID): ProductCategory? =
        jpaRepository.findById(id).orElse(null)?.toDomain()

    override fun findByName(name: String): ProductCategory? =
        jpaRepository.findByNameIgnoreCase(name)?.toDomain()

    override fun findAll(pageable: Pageable): Page<ProductCategory> =
        jpaRepository.findAll(pageable).map { it.toDomain() }

    override fun save(category: ProductCategory): ProductCategory {
        val entity = jpaRepository.findById(category.id).orElse(null)
            ?: ProductCategoryJpaEntity(id = category.id, name = category.name, description = category.description)
        entity.name = category.name
        entity.description = category.description
        return jpaRepository.save(entity).toDomain()
    }
}
