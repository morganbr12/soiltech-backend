package com.soiltech.backend.infrastructure.persistence.adapter

import com.soiltech.backend.domain.entity.AdminProfile
import com.soiltech.backend.domain.repository.AdminProfileRepository
import com.soiltech.backend.infrastructure.persistence.entity.AdminProfileJpaEntity
import com.soiltech.backend.infrastructure.persistence.jpa.AdminProfileJpaRepository
import com.soiltech.backend.infrastructure.persistence.jpa.AdminRoleJpaRepository
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class AdminProfileRepositoryAdapter(
    private val profileJpaRepository: AdminProfileJpaRepository,
    private val roleJpaRepository: AdminRoleJpaRepository
) : AdminProfileRepository {

    override fun findByUserId(userId: UUID): AdminProfile? =
        profileJpaRepository.findByUserId(userId)?.toDomain()

    override fun findById(id: UUID): AdminProfile? =
        profileJpaRepository.findById(id).orElse(null)?.toDomain()

    override fun existsByUserId(userId: UUID): Boolean =
        profileJpaRepository.existsByUserId(userId)

    override fun save(profile: AdminProfile): AdminProfile {
        val roleEntity = roleJpaRepository.findById(profile.adminRoleId).orElseThrow {
            IllegalArgumentException("Admin role ${profile.adminRoleId} not found")
        }
        val existing = profileJpaRepository.findByUserId(profile.userId)
        val entity = if (existing != null) {
            existing.adminRole = roleEntity
            existing.fullName = profile.fullName
            existing.phone = profile.phone
            existing.region = profile.region
            existing.isActive = profile.isActive
            existing
        } else {
            AdminProfileJpaEntity(
                id = profile.id,
                userId = profile.userId,
                fullName = profile.fullName,
                email = profile.email,
                phone = profile.phone,
                region = profile.region,
                adminRole = roleEntity,
                isActive = profile.isActive
            )
        }
        return profileJpaRepository.save(entity).toDomain()
    }

    override fun findAll(): List<AdminProfile> =
        profileJpaRepository.findAll().map { it.toDomain() }
}
