package com.soiltech.backend.infrastructure.persistence.jpa

import com.soiltech.backend.infrastructure.persistence.entity.CustomerProfileJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface CustomerProfileJpaRepository : JpaRepository<CustomerProfileJpaEntity, UUID> {
    fun findByUserId(userId: UUID): CustomerProfileJpaEntity?
}
