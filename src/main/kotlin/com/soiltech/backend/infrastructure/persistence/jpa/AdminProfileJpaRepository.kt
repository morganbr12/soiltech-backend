package com.soiltech.backend.infrastructure.persistence.jpa

import com.soiltech.backend.infrastructure.persistence.entity.AdminProfileJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface AdminProfileJpaRepository : JpaRepository<AdminProfileJpaEntity, UUID> {
    fun findByUserId(userId: UUID): AdminProfileJpaEntity?
    fun existsByUserId(userId: UUID): Boolean
}
