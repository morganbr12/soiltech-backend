package com.soiltech.backend.infrastructure.persistence.jpa

import com.soiltech.backend.infrastructure.persistence.entity.ProductJpaEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ProductJpaRepository : JpaRepository<ProductJpaEntity, UUID> {

    @Query("""
        SELECT p FROM ProductJpaEntity p
        WHERE p.isAvailable = true
          AND (:categoryId IS NULL OR p.categoryId = :categoryId)
          AND (:query IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')))
        ORDER BY p.createdAt DESC
    """)
    fun findAllFiltered(
        @Param("categoryId") categoryId: UUID?,
        @Param("query") query: String?,
        pageable: Pageable
    ): Page<ProductJpaEntity>

    fun findByIsOnDealTrueAndIsAvailableTrue(pageable: Pageable): Page<ProductJpaEntity>

    fun findByIsFeaturedTrueAndIsAvailableTrue(pageable: Pageable): Page<ProductJpaEntity>
}
