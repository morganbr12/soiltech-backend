package com.soiltech.backend.infrastructure.persistence.adapter

import com.soiltech.backend.domain.entity.User
import com.soiltech.backend.domain.repository.UserRepository
import com.soiltech.backend.infrastructure.persistence.entity.UserJpaEntity
import com.soiltech.backend.infrastructure.persistence.jpa.UserJpaRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Component
class UserRepositoryAdapter(
    private val jpaRepository: UserJpaRepository
) : UserRepository {

    override fun findById(id: UUID): User? =
        jpaRepository.findById(id).orElse(null)?.toDomain()

    override fun findByEmail(email: String): User? =
        jpaRepository.findByEmail(email)?.toDomain()

    override fun findByPhone(phone: String): User? =
        jpaRepository.findByPhone(phone)?.toDomain()

    override fun save(user: User): User {
        val entity = jpaRepository.findById(user.id).orElse(UserJpaEntity.fromDomain(user))
        entity.apply {
            passwordHash = user.passwordHash
            phone = user.phone
            lastLoginAt = user.lastLoginAt
        }
        return jpaRepository.save(entity).toDomain()
    }

    @Transactional
    override fun updateLastLogin(userId: UUID, time: LocalDateTime) {
        val entity = jpaRepository.findById(userId).orElse(null) ?: return
        entity.lastLoginAt = time
        jpaRepository.save(entity)
    }

    override fun existsByEmail(email: String): Boolean =
        jpaRepository.existsByEmail(email)

    override fun existsByPhone(phone: String): Boolean =
        jpaRepository.existsByPhone(phone)

    override fun delete(id: UUID) = jpaRepository.deleteById(id)
}
