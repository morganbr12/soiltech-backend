package com.soiltech.backend.infrastructure.persistence.jpa

import com.soiltech.backend.domain.enum.AdminRoleName
import com.soiltech.backend.infrastructure.persistence.entity.AdminProfileJpaEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface AdminProfileJpaRepository : JpaRepository<AdminProfileJpaEntity, UUID> {
    fun findByUserId(userId: UUID): AdminProfileJpaEntity?
    fun existsByUserId(userId: UUID): Boolean

    @Query("""
        SELECT p FROM AdminProfileJpaEntity p
        WHERE (:role IS NULL OR p.adminRole.name = :role)
          AND (:isActive IS NULL OR p.isActive = :isActive)
          AND (:search IS NULL OR LOWER(p.fullName) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(p.email) LIKE LOWER(CONCAT('%', :search, '%')))
        ORDER BY p.createdAt DESC
    """)
    fun findAllFiltered(
        @Param("role") role: AdminRoleName?,
        @Param("isActive") isActive: Boolean?,
        @Param("search") search: String?,
        pageable: Pageable
    ): Page<AdminProfileJpaEntity>
}
