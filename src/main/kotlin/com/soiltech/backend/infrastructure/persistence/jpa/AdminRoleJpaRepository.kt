package com.soiltech.backend.infrastructure.persistence.jpa

import com.soiltech.backend.domain.enum.AdminRoleName
import com.soiltech.backend.infrastructure.persistence.entity.AdminRoleJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface AdminRoleJpaRepository : JpaRepository<AdminRoleJpaEntity, UUID> {
    fun findByName(name: AdminRoleName): AdminRoleJpaEntity?
}
