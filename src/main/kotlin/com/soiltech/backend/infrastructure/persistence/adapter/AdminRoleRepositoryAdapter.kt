package com.soiltech.backend.infrastructure.persistence.adapter

import com.soiltech.backend.domain.entity.AdminRole
import com.soiltech.backend.domain.enum.AdminRoleName
import com.soiltech.backend.domain.repository.AdminRoleRepository
import com.soiltech.backend.infrastructure.persistence.jpa.AdminRoleJpaRepository
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class AdminRoleRepositoryAdapter(
    private val jpaRepository: AdminRoleJpaRepository
) : AdminRoleRepository {

    override fun findAll(): List<AdminRole> = jpaRepository.findAll().map { it.toDomain() }

    override fun findById(id: UUID): AdminRole? = jpaRepository.findById(id).orElse(null)?.toDomain()

    override fun findByName(name: AdminRoleName): AdminRole? = jpaRepository.findByName(name)?.toDomain()
}
