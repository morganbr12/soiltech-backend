package com.soiltech.backend.infrastructure.persistence.jpa

import com.soiltech.backend.infrastructure.persistence.entity.FarmJpaEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface FarmJpaRepository : JpaRepository<FarmJpaEntity, UUID> {
    fun findByFarmerId(farmerId: UUID, pageable: Pageable): Page<FarmJpaEntity>
}
