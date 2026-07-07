package com.soiltech.backend.infrastructure.persistence.adapter

import com.soiltech.backend.domain.entity.CustomerProfile
import com.soiltech.backend.domain.repository.CustomerProfileRepository
import com.soiltech.backend.infrastructure.persistence.entity.CustomerProfileJpaEntity
import com.soiltech.backend.infrastructure.persistence.jpa.CustomerProfileJpaRepository
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class CustomerProfileRepositoryAdapter(
    private val jpaRepository: CustomerProfileJpaRepository
) : CustomerProfileRepository {

    override fun findById(id: UUID): CustomerProfile? =
        jpaRepository.findById(id).orElse(null)?.toDomain()

    override fun findByUserId(userId: UUID): CustomerProfile? =
        jpaRepository.findByUserId(userId)?.toDomain()

    override fun save(profile: CustomerProfile): CustomerProfile =
        jpaRepository.save(CustomerProfileJpaEntity.fromDomain(profile)).toDomain()

    override fun update(profile: CustomerProfile): CustomerProfile {
        val entity = jpaRepository.findById(profile.id).orElseThrow()
        entity.apply {
            fullName = profile.fullName
            phone = profile.phone
            address = profile.address
        }
        return jpaRepository.save(entity).toDomain()
    }
}
